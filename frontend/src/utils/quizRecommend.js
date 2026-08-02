import { fetchMyQuizResult } from '@/api/khachHangApi'
import { isCustomerLoggedIn } from '@/composables/useAuth'

export const QUIZ_PROFILE_KEY = 'sunova_quiz_profile'

/**
 * @typedef {{ idLoaiDa: number, tenLoaiDa?: string, scoreMap: Record<string|number, { name?: string, points: number }>, filters?: string[] }} QuizProfile
 */

/** @param {QuizProfile} profile */
export function saveQuizProfile(profile) {
  if (!profile?.idLoaiDa) return
  try {
    localStorage.setItem(
      QUIZ_PROFILE_KEY,
      JSON.stringify({
        idLoaiDa: Number(profile.idLoaiDa),
        tenLoaiDa: profile.tenLoaiDa || '',
        scoreMap: profile.scoreMap || {},
        filters: Array.isArray(profile.filters) ? profile.filters : [],
        savedAt: Date.now(),
      }),
    )
  } catch {
    // ignore quota / private mode
  }
}

/** @returns {QuizProfile|null} */
export function loadQuizProfile() {
  try {
    const raw = localStorage.getItem(QUIZ_PROFILE_KEY)
    if (!raw) return null
    const data = JSON.parse(raw)
    if (!data?.idLoaiDa) return null
    return {
      idLoaiDa: Number(data.idLoaiDa),
      tenLoaiDa: data.tenLoaiDa || '',
      scoreMap: data.scoreMap || {},
      filters: Array.isArray(data.filters) ? data.filters : [],
    }
  } catch {
    return null
  }
}

export function clearQuizProfile() {
  try {
    localStorage.removeItem(QUIZ_PROFILE_KEY)
  } catch {
    // ignore
  }
}

/**
 * Resolve quiz profile: localStorage first; if empty and logged in, fetch API and cache.
 * @returns {Promise<QuizProfile|null>}
 */
export async function resolveQuizProfile() {
  const local = loadQuizProfile()
  if (local) return local

  if (!isCustomerLoggedIn()) return null

  try {
    const res = await fetchMyQuizResult()
    const data = res.data
    if (!data?.idLoaiDa) return null

    const profile = {
      idLoaiDa: Number(data.idLoaiDa),
      tenLoaiDa: data.tenLoaiDa || '',
      scoreMap: {
        [data.idLoaiDa]: { name: data.tenLoaiDa || '', points: 1 },
      },
      filters: [],
    }
    saveQuizProfile(profile)
    return profile
  } catch {
    return null
  }
}

/**
 * Rank products by quiz scoreMap + hard filters (same logic as quiz result page).
 * Returns full sorted list (no top-N cut).
 */
export function rankProductsByQuiz(products, { scoreMap = {}, filters = [] } = {}) {
  const list = Array.isArray(products) ? products : []
  const scores = scoreMap || {}

  const scoredProducts = list.map((product) => {
    let matchScore = 0
    if (product.idLoaiDas && Array.isArray(product.idLoaiDas)) {
      product.idLoaiDas.forEach((loaiDaId) => {
        const entry = scores[loaiDaId] ?? scores[String(loaiDaId)]
        if (entry?.points) matchScore += entry.points
      })
    }
    return { ...product, matchScore }
  })

  let suitableProducts = scoredProducts.filter((p) => p.matchScore > 0)
  if (suitableProducts.length === 0) {
    suitableProducts = scoredProducts
  }

  if (filters.length > 0) {
    const filtered = suitableProducts.filter((p) => {
      if (!p.loaiChongNang) return false
      return filters.some((f) => {
        if (f === 'VAT_LY') return p.loaiChongNang === 'VAT_LY' || p.loaiChongNang === 'LAI'
        if (f === 'HOA_HOC') return p.loaiChongNang === 'HOA_HOC' || p.loaiChongNang === 'LAI'
        return p.loaiChongNang === f
      })
    })
    if (filtered.length > 0) suitableProducts = filtered
  }

  suitableProducts.sort((a, b) => b.matchScore - a.matchScore)
  return suitableProducts
}
