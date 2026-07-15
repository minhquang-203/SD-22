import { computed, defineComponent, h } from 'vue'
import { NIcon } from 'naive-ui'
import { Icon } from '@iconify/vue'
import { useAdminBadges } from '@/composables/useAdminBadges'

function renderMenuIcon(icon) {
  if (!icon) return undefined
  return () => h(NIcon, { size: 18 }, { default: () => h(Icon, { icon }) })
}

function renderGroupLabel(label, collapsed) {
  if (collapsed) {
    return () =>
      h('span', {
        class: 'admin-menu-group-divider',
        'aria-hidden': 'true',
      })
  }
  return label
}

/** Component nhỏ — reactive với badge store, không phụ thuộc NMenu remount */
const MenuLabelBadge = defineComponent({
  name: 'MenuLabelBadge',
  props: {
    label: { type: String, required: true },
    path: { type: String, default: '' },
  },
  setup(props) {
    const { sidebarBadgeByPath, formatBadge } = useAdminBadges()
    const text = computed(() =>
      formatBadge(props.path ? sidebarBadgeByPath.value[props.path] : 0),
    )
    return () =>
      h('span', { class: 'admin-menu-label' }, [
        h('span', { class: 'admin-menu-label__text' }, props.label),
        text.value ? h('span', { class: 'admin-menu-badge' }, text.value) : null,
      ])
  },
})

function renderLabelWithBadge(label, path) {
  return () => h(MenuLabelBadge, { label, path: path || '' })
}

export function buildAdminMenuOptions(items, collapsed = false) {
  return items.map((item) => {
    if (item.type === 'group') {
      return {
        type: 'group',
        key: item.key,
        label: renderGroupLabel(item.label, collapsed),
        children: buildAdminMenuOptions(item.children || [], collapsed),
      }
    }

    const optionKey = item.path || item.key
    const option = {
      key: optionKey,
      icon: renderMenuIcon(item.icon),
      disabled: !!item.disabled && !item.path && !item.children?.length,
      label: renderLabelWithBadge(item.label, item.path),
    }

    if (item.children?.length) {
      option.children = buildAdminMenuOptions(item.children, collapsed)
    }

    return option
  })
}
