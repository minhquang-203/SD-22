/**
 * Dữ liệu bài viết Tin tức & Cẩm nang (storefront).
 * Nội dung tự viết — tham khảo nguồn, không sao chép nguyên văn.
 */

export const TIN_TUC_DANH_MUC = [
  'Tất cả',
  'Kiến thức chống nắng',
  'Hướng dẫn chọn sản phẩm',
  'Tin SUNOVA',
]

/** @typedef {{ ten: string, url: string } | null} TinTucNguon */

/**
 * @typedef {object} TinTucBai
 * @property {number} id
 * @property {string} slug
 * @property {string} tieuDe
 * @property {string} danhMuc
 * @property {string} ngay ISO date
 * @property {string|null} anhBia
 * @property {string} tomTat
 * @property {string[]} noiDung
 * @property {TinTucNguon} nguon
 * @property {boolean} noiBat
 * @property {string} [icon]
 * @property {{ label: string, to: string }|null} [ctaNoiBo]
 */

/** @type {TinTucBai[]} */
export const tinTucBaiViet = [
  {
    id: 1,
    slug: 'spf-va-pa-la-gi-doc-dung-ky-hieu',
    tieuDe: 'SPF và PA là gì? Đọc đúng ký hiệu trên kem chống nắng',
    danhMuc: 'Kiến thức chống nắng',
    ngay: '2026-03-12',
    anhBia: null,
    icon: 'shield',
    tomTat:
      'Hai dòng chữ nhỏ trên tuýp kem — SPF và PA — nói lên khả năng bảo vệ tia UVB và UVA. Biết đọc đúng giúp bạn chọn sản phẩm phù hợp hơn.',
    noiDung: [
      'Trên bao bì kem chống nắng, bạn thường thấy SPF kèm một con số và PA kèm dấu cộng. Đây không phải trang trí — chúng cho biết mức bảo vệ trước các loại tia mặt trời khác nhau.',
      'SPF (Sun Protection Factor) chủ yếu phản ánh khả năng giảm tác động của tia UVB — loại tia dễ gây cháy nắng, đỏ da. Số càng cao thì thời gian da chịu được UVB dài hơn so với không bôi, trong điều kiện dùng đúng lượng.',
      'PA (Protection Grade of UVA) dùng hệ dấu “+” để ước lượng bảo vệ trước tia UVA — tia xuyên sâu hơn, liên quan đến lão hóa sớm và sạm da. PA+, PA++, PA+++, PA++++ lần lượt thể hiện mức bảo vệ tăng dần.',
      'Khi mua kem, hãy nhìn cả hai chỉ số thay vì chỉ chăm chăm SPF. Một sản phẩm “broad spectrum” (phổ rộng) thường được thiết kế để cân bằng cả UVB và UVA.',
      'Nhớ rằng chỉ số trên bao bì chỉ phát huy khi bạn bôi đủ lượng và thoa lại đúng lúc — nhất là sau khi đổ mồ hôi hoặc tiếp xúc nước.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/biet-cach-doc-cac-ky-hieu-de-chon-kem-chong-nang-phu-hop-vi',
    },
    noiBat: true,
    ctaNoiBo: null,
  },
  {
    id: 2,
    slug: 'ngoai-spf-vi-sao-can-de-y-chi-so-pa',
    tieuDe: 'Ngoài SPF, vì sao cần để ý chỉ số PA?',
    danhMuc: 'Kiến thức chống nắng',
    ngay: '2026-03-08',
    anhBia: null,
    icon: 'sun',
    tomTat:
      'SPF bảo vệ khỏi cháy nắng; PA nhắc bạn về tia UVA — “kẻ thầm lặng” vẫn xuyên qua kính và mây. Bỏ qua PA là bỏ nửa bức tranh bảo vệ da.',
    noiDung: [
      'Nhiều người chỉ nhớ SPF vì nó gắn với cảm giác “không bị cháy nắng”. Nhưng da còn chịu tia UVA hầu như quanh năm — kể cả ngày dịu nắng hay ngồi gần cửa kính.',
      'UVA ít gây đỏ ngay lập tức nhưng góp phần làm da mất đàn hồi, xuất hiện nếp nhăn và đốm nâu theo thời gian. Chỉ số PA giúp bạn ước lượng mức “lá chắn” với nhóm tia này.',
      'Một kem SPF cao nhưng PA thấp có thể “đủ” cho buổi ngoài trời ngắn về UVB, song chưa chắc đủ nếu bạn cần bảo vệ lâu dài trước lão hóa do nắng.',
      'Khi chọn sản phẩm hằng ngày, ưu tiên công thức có SPF phù hợp nhu cầu và PA từ +++ trở lên nếu bạn thường xuyên ra ngoài hoặc ở vùng nắng mạnh.',
      'Kết hợp kem chống nắng với mũ, kính và tránh nắng giữa trưa vẫn là lớp bảo vệ thực tế nhất — chỉ số trên tuýp không thay thế thói quen lành mạnh.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/chon-kem-chong-nang-ngoai-chi-so-spf-vi-sao-can-chu-y-chi-so-pa-vi',
    },
    noiBat: false,
    ctaNoiBo: null,
  },
  {
    id: 3,
    slug: 'spf-bao-nhieu-la-du',
    tieuDe: 'SPF bao nhiêu là đủ? Không phải càng cao càng tốt',
    danhMuc: 'Hướng dẫn chọn sản phẩm',
    ngay: '2026-02-28',
    anhBia: null,
    icon: 'thermometer',
    tomTat:
      'SPF 100 không đồng nghĩa bảo vệ gấp đôi SPF 50. Chọn mức phù hợp thói quen ngoài trời và bôi đúng cách quan trọng hơn chạy theo số “khủng”.',
    noiDung: [
      'SPF đo mức giảm UVB tương đối, không phải tỷ lệ bảo vệ tuyến tính. Ví dụ SPF 30 đã chặn phần lớn UVB trong điều kiện lý tưởng; SPF 50 cao hơn một chút chứ không phải “gấp đôi độ an toàn”.',
      'Với đi làm, học tập trong đô thị, nhiều chuyên gia da liễu coi SPF 30–50 là ngưỡng thực tế nếu bạn bôi đủ và thoa lại. SPF rất cao hữu ích hơn khi leo núi, biển hoặc nắng gay gắt kéo dài.',
      'Số lớn trên nhãn dễ tạo cảm giác “bôi một lần là xong cả ngày” — đó là hiểu nhầm. Kem vẫn bị mài mòn bởi mồ hôi, ma sát khăn và nước.',
      'Hãy cân nhắc kết cấu: kem SPF cao đôi khi đặc hơn. Nếu da dầu, chọn bản gel/fluid SPF 50 để dễ dùng đều đặn mỗi sáng còn hơn SPF 100 nằm nguyên trong tủ.',
      'Tóm lại: chọn SPF theo môi trường sống, ưu tiên công thức bạn chịu đựng được mỗi ngày, và luôn nhớ thoa lại sau 2–3 giờ khi ở ngoài nắng.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/kem-chong-nang-chi-so-spfpa-bao-nhieu-la-tot-vi',
    },
    noiBat: false,
    ctaNoiBo: null,
  },
  {
    id: 4,
    slug: 'chon-kem-chong-nang-theo-loai-da',
    tieuDe: 'Chọn kem chống nắng theo loại da: dầu, khô, hỗn hợp',
    danhMuc: 'Hướng dẫn chọn sản phẩm',
    ngay: '2026-02-20',
    anhBia: null,
    icon: 'droplet',
    tomTat:
      'Cùng SPF 50 nhưng gel cho da dầu và cream cho da khô cho cảm giác hoàn toàn khác. Khớp kết cấu với loại da giúp bạn duy trì thói quen bôi mỗi sáng.',
    noiDung: [
      'Da dầu và da mụn thường hợp công thức nhẹ: gel, fluid, hoặc base “oil-free / non-comedogenic”. Kết cấu quá đặc dễ bóng nhờn và làm bạn ngại thoa lại giữa ngày.',
      'Da khô cần lớp dưỡng ẩm trước hoặc kem chống nắng dạng cream/lotion. Thiếu ẩm khiến da căng, bong và dễ bỏ cuộc với routine chống nắng.',
      'Da hỗn hợp: ưu tiên bản cân bằng hoặc dùng lượng vừa phải vùng chữ T, thêm lớp mỏng vùng hai má nếu cần. Quan trọng là phủ đều, không để “mảng trống”.',
      'Ngoài loại da, hãy xem bạn hay ở trong nhà hay ngoài trời, có trang điểm hay không — các yếu tố này quyết định bạn chọn bản tint, matte hay glow.',
      'Nếu đang điều trị da liễu, hỏi bác sĩ trước khi đổi kem. Một số hoạt chất làm da nhạy cảm hơn với nắng và cần SPF ổn định mỗi ngày.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/su-dung-kem-chong-nang-sao-cho-phu-hop-vi',
    },
    noiBat: false,
    ctaNoiBo: null,
  },
  {
    id: 5,
    slug: 'tia-uva-va-uvb-khac-nhau-the-nao',
    tieuDe: 'Tia UVA và UVB khác nhau thế nào?',
    danhMuc: 'Kiến thức chống nắng',
    ngay: '2026-02-14',
    anhBia: null,
    icon: 'sun',
    tomTat:
      'UVB “đốt” bề mặt gây cháy nắng; UVA “xuyên” sâu hơn và hiện diện hầu như cả ngày. Hiểu hai loại tia giúp bạn chọn kem phổ rộng có chủ đích.',
    noiDung: [
      'Ánh nắng mặt trời mang theo nhiều bước sóng; với da, hai nhóm được nhắc nhiều nhất là UVB và UVA. Chúng khác nhau về năng lượng, độ xuyên sâu và hậu quả trên da.',
      'UVB chủ yếu tác động lớp ngoài, gây đỏ, bỏng nắng và là yếu tố nguy cơ ung thư da theo thời gian phơi nhiễm. SPF phản ánh phần lớn khả năng chống nhóm tia này.',
      'UVA có bước sóng dài hơn, xuyên kính cửa sổ và mây tốt hơn, góp phần phá hủy collagen và tạo sắc tố. Đó là lý do PA và nhãn “broad spectrum” đáng để đọc kỹ.',
      'Ngày nhiều mây không đồng nghĩa “không cần kem”. Một phần UV vẫn tới da; thói quen bôi buổi sáng vẫn hữu ích ngay cả khi trời dịu.',
      'Bảo vệ tốt nhất là kết hợp: kem phổ rộng, trang phục, tìm bóng râm lúc nắng đỉnh điểm — hơn là chỉ dựa vào một chỉ số duy nhất trên nhãn.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/cach-chon-kem-chong-nang-bao-ve-da-khoi-tia-uva-va-uvb-vi',
    },
    noiBat: false,
    ctaNoiBo: null,
  },
  {
    id: 6,
    slug: 'chi-so-pa-co-may-cap-do',
    tieuDe: 'Chỉ số PA có mấy cấp độ?',
    danhMuc: 'Kiến thức chống nắng',
    ngay: '2026-02-05',
    anhBia: null,
    icon: 'map',
    tomTat:
      'Từ PA+ đến PA++++, mỗi cấp thể hiện mức bền vững trước tia UVA khác nhau. Biết thang đo giúp bạn so sánh sản phẩm nhanh trên kệ.',
    noiDung: [
      'Hệ PA phổ biến tại nhiều thị trường châu Á dùng dấu cộng để mô tả mức bảo vệ UVA. Thường gặp bốn cấp: PA+, PA++, PA+++ và PA++++.',
      'PA+ là mức cơ bản; càng nhiều dấu cộng, khả năng duy trì bảo vệ UVA càng được đánh giá cao hơn trong khung thử nghiệm của nhà sản xuất.',
      'Khi so hai tuýp cùng SPF, ưu tiên PA cao hơn nếu bạn làm việc gần cửa kính, lái xe nhiều hoặc sống ở vùng nắng quanh năm như Việt Nam.',
      'PA không thay thế SPF. Hai chỉ số bổ sung nhau: một cái “gác cửa” UVB, một cái hỗ trợ UVA. Đọc cả hai trước khi quyết định mua.',
      'Dù PA++++, kem vẫn cần lượng đủ và thoa lại. Chỉ số cao không miễn trừ thói quen sử dụng đúng cách.',
    ],
    nguon: {
      ten: 'Vinmec',
      url: 'https://www.vinmec.com/vie/bai-viet/chi-so-pa-trong-kem-chong-nang-co-may-cap-do-y-nghia-cua-chi-so-pa-vi',
    },
    noiBat: false,
    ctaNoiBo: null,
  },
  {
    id: 7,
    slug: 'hieu-dung-spf-pa-de-chon-kem',
    tieuDe: 'Hiểu đúng SPF, PA để chọn kem chống nắng',
    danhMuc: 'Kiến thức chống nắng',
    ngay: '2026-01-28',
    anhBia: null,
    icon: 'shield',
    tomTat:
      'Gom lại trong một bài: SPF nói về UVB, PA nói về UVA, và cách chọn mức phù hợp nhịp sống nhiệt đới của bạn.',
    noiDung: [
      'Chọn kem chống nắng không cần thuộc lòng bảng số học phức tạp. Chỉ cần nắm: SPF gắn với cháy nắng (UVB), PA gắn với UVA và lão hóa sớm do nắng.',
      'Với khí hậu Việt Nam, nhiều người cảm thấy an tâm hơn với SPF 50 và PA+++ trở lên cho ngày ra ngoài nhiều. Ngày chủ yếu trong nhà vẫn nên duy trì lớp mỏng buổi sáng.',
      'Đọc thêm nhãn “broad spectrum”, chống nước (nếu bơi/đổ mồ hôi) và kết cấu hợp da. Một tuýp “đủ số” nhưng bạn ghét cảm giác sẽ sớm bị bỏ quên.',
      'Thoa khoảng hai ngón tay cho mặt và cổ, đợi vài phút trước trang điểm, thoa lại sau 2 giờ nếu còn ngoài nắng — đó là phần “thực chiến” quan trọng không kém chỉ số.',
      'Khi phân vân giữa nhiều lựa chọn, hãy bắt đầu từ loại da và thói quen ngoài trời, rồi mới tinh chỉnh SPF/PA. SUNOVA có quiz ngắn nếu bạn muốn gợi ý nhanh hơn.',
    ],
    nguon: {
      ten: 'Báo Thanh Niên',
      url: 'https://thanhnien.vn/chi-so-spf-va-pa-trong-kem-chong-nang-la-gi-va-cach-lua-chon-phu-hop-1851446701.htm',
    },
    noiBat: false,
    ctaNoiBo: { label: 'Làm quiz chọn kem', to: '/quiz' },
  },
  {
    id: 8,
    slug: 'lam-quiz-da-2-phut',
    tieuDe: 'Làm quiz da 2 phút — tìm kem chống nắng hợp với bạn',
    danhMuc: 'Tin SUNOVA',
    ngay: '2026-03-18',
    anhBia: null,
    icon: 'droplet',
    tomTat:
      'Trả lời vài câu về loại da, thói quen ngoài trời và sở thích kết cấu — SUNOVA gợi ý sản phẩm gần với nhu cầu thực tế của bạn.',
    noiDung: [
      'Không phải ai cũng muốn đọc hết bảng thành phần trước khi mua kem. Quiz da của SUNOVA rút gọn hành trình đó thành vài lựa chọn rõ ràng trong khoảng hai phút.',
      'Bạn sẽ được hỏi về độ dầu/khô, mức độ ra nắng, và cảm giác kem bạn thích (nhẹ, dưỡng ẩm, hoặc hợp trang điểm). Hệ thống xếp hạng sản phẩm dựa trên các tín hiệu đó.',
      'Kết quả chỉ mang tính gợi ý mua sắm — không phải chẩn đoán da liễu. Nếu da đang kích ứng hoặc điều trị chuyên khoa, hãy ưu tiên lời khuyên của bác sĩ.',
      'Sau quiz, bạn có thể xem ngay danh sách gợi ý hoặc tiếp tục duyệt toàn bộ bộ sưu tập chống nắng trên cửa hàng.',
    ],
    nguon: null,
    noiBat: false,
    ctaNoiBo: { label: 'Bắt đầu quiz ngay', to: '/quiz' },
  },
  {
    id: 9,
    slug: 'uu-dai-mua-he-bo-suu-tap-chong-nang',
    tieuDe: 'Ưu đãi mùa hè: giảm giá bộ sưu tập chống nắng',
    danhMuc: 'Tin SUNOVA',
    ngay: '2026-03-15',
    anhBia: null,
    icon: 'sun',
    tomTat:
      'Mùa nắng cao điểm là lúc “lá chắn” da cần sẵn sàng. Khám phá các sản phẩm đang giảm trong bộ sưu tập chống nắng SUNOVA.',
    noiDung: [
      'Khi nhiệt độ và chỉ số UV tăng, nhu cầu kem chống nắng hằng ngày cũng tăng theo. SUNOVA gom các ưu đãi theo mùa để bạn dễ chọn lại tuýp phù hợp trước khi hết hàng.',
      'Bạn có thể lọc sản phẩm đang khuyến mãi, xem SPF/PA và đọc đánh giá nhanh trước khi thêm vào giỏ. Ưu đãi có thể thay đổi theo từng đợt — hãy kiểm tra giá hiện tại trên trang sản phẩm.',
      'Mẹo nhỏ: mua đúng kết cấu bạn đã quen dùng sẽ giúp routine không bị gián đoạn giữa mùa. Nếu muốn đổi loại, hãy thử dung tích nhỏ trước khi mua bản lớn.',
      'Ghé trang khuyến mãi để xem danh sách cập nhật và kết hợp với voucher (nếu có) khi thanh toán.',
    ],
    nguon: null,
    noiBat: false,
    ctaNoiBo: { label: 'Xem sản phẩm khuyến mãi', to: '/san-pham/khuyen-mai' },
  },
  {
    id: 10,
    slug: 'sunova-cam-ket-hang-chinh-hang',
    tieuDe: 'SUNOVA cam kết 100% hàng chính hãng, rõ lô và hạn sử dụng',
    danhMuc: 'Tin SUNOVA',
    ngay: '2026-03-01',
    anhBia: null,
    icon: 'shield',
    tomTat:
      'Mỗi sản phẩm trên SUNOVA đi kèm thông tin rõ ràng về nguồn gốc và hạn dùng — để bạn yên tâm bảo vệ da mỗi ngày.',
    noiDung: [
      'Chống nắng là sản phẩm dùng sát da mỗi ngày. Vì vậy SUNOVA đặt ưu tiên vào hàng chính hãng và quy trình nhập — xuất kho có kiểm soát lô, hạn sử dụng.',
      'Trên hóa đơn và phiếu giao, bạn có thể đối chiếu thông tin đơn hàng. Với bán tại quầy, nhân viên hỗ trợ kiểm tra hạn trước khi mang về.',
      'Nếu nhận sản phẩm nghi ngờ tem, bao bì hoặc hạn dùng, hãy liên hệ CSKH sớm để được hỗ trợ theo chính sách đổi trả của cửa hàng.',
      'Tìm hiểu thêm câu chuyện thương hiệu và cam kết chất lượng trên trang Giới thiệu SUNOVA.',
    ],
    nguon: null,
    noiBat: false,
    ctaNoiBo: { label: 'Tìm hiểu về SUNOVA', to: '/gioi-thieu' },
  },
]

export function getTinTucBySlug(slug) {
  return tinTucBaiViet.find((b) => b.slug === slug) || null
}

export function getBaiNoiBat() {
  return tinTucBaiViet.find((b) => b.noiBat) || tinTucBaiViet[0] || null
}

export function getBaiLienQuan(slug, limit = 3) {
  const current = getTinTucBySlug(slug)
  if (!current) return []
  return tinTucBaiViet
    .filter((b) => b.slug !== slug && b.danhMuc === current.danhMuc)
    .slice(0, limit)
}

export function getBaiMoiNhat(limit = 3) {
  return [...tinTucBaiViet]
    .sort((a, b) => String(b.ngay).localeCompare(String(a.ngay)))
    .slice(0, limit)
}

export function formatTinTucDate(iso) {
  if (!iso) return ''
  const d = new Date(`${iso}T00:00:00`)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}
