import { h } from 'vue'
import { RouterLink } from 'vue-router'
import { NIcon } from 'naive-ui'
import { Icon } from '@iconify/vue'

function renderMenuIcon(icon) {
  if (!icon) return undefined
  return () => h(NIcon, { size: 18 }, { default: () => h(Icon, { icon }) })
}

export function buildAdminMenuOptions(items) {
  return items.map((item) => {
    const option = {
      key: item.path || item.key,
      icon: renderMenuIcon(item.icon),
      disabled: !!item.disabled && !item.path && !item.children?.length,
    }

    if (item.path && !item.disabled) {
      option.label = () =>
        h(
          RouterLink,
          { to: item.path, class: 'admin-menu-router-link' },
          { default: () => item.label },
        )
    } else {
      option.label = item.label
    }

    if (item.children?.length) {
      option.children = buildAdminMenuOptions(item.children)
    }

    return option
  })
}
