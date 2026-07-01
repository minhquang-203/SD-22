<template>
  <svg width="0" height="0" style="position: absolute" aria-hidden="true">
    <defs>
      <symbol id="blog-icon-sun" viewBox="0 0 48 48">
        <circle cx="24" cy="24" r="9" stroke="currentColor" stroke-width="1.6" />
        <g stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
          <line x1="24" y1="4" x2="24" y2="10" />
          <line x1="24" y1="38" x2="24" y2="44" />
          <line x1="4" y1="24" x2="10" y2="24" />
          <line x1="38" y1="24" x2="44" y2="24" />
          <line x1="9.5" y1="9.5" x2="13.6" y2="13.6" />
          <line x1="34.4" y1="34.4" x2="38.5" y2="38.5" />
          <line x1="9.5" y1="38.5" x2="13.6" y2="34.4" />
          <line x1="34.4" y1="13.6" x2="38.5" y2="9.5" />
        </g>
      </symbol>

      <symbol id="blog-icon-cloud-rain" viewBox="0 0 48 48">
        <path
          d="M14.5 28a7.5 7.5 0 0 1 1-14.9A9.5 9.5 0 0 1 34 15.8 6.5 6.5 0 0 1 33 28.5H14.5Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linejoin="round"
        />
        <g stroke="currentColor" stroke-width="1.6" stroke-linecap="round">
          <line x1="18" y1="34" x2="16" y2="40" />
          <line x1="25" y1="34" x2="23" y2="40" />
          <line x1="32" y1="34" x2="30" y2="40" />
        </g>
      </symbol>

      <symbol id="blog-icon-shield" viewBox="0 0 48 48">
        <path
          d="M24 5 9 11v11c0 10.5 6.4 18.5 15 21 8.6-2.5 15-10.5 15-21V11L24 5Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linejoin="round"
        />
        <path
          d="M17 23.5 22 28.5 31.5 18"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </symbol>

      <symbol id="blog-icon-droplet" viewBox="0 0 48 48">
        <path
          d="M24 6c6 8.5 12 15.7 12 22.5C36 36.6 30.6 42 24 42S12 36.6 12 28.5C12 21.7 18 14.5 24 6Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linejoin="round"
        />
        <path
          d="M18 28a6 6 0 0 0 6 6"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
        />
      </symbol>

      <symbol id="blog-icon-thermometer" viewBox="0 0 48 48">
        <path
          d="M21 8a3 3 0 0 1 6 0v19.6a8 8 0 1 1-6 0V8Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linejoin="round"
        />
        <circle cx="24" cy="33" r="3" fill="currentColor" />
        <line
          x1="24"
          y1="14"
          x2="24"
          y2="27"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
        />
      </symbol>

      <symbol id="blog-icon-map" viewBox="0 0 48 48">
        <path
          d="M24 6c6.6 0 12 5.3 12 11.8C36 26.6 24 42 24 42S12 26.6 12 17.8C12 11.3 17.4 6 24 6Z"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linejoin="round"
        />
        <circle cx="24" cy="18" r="4.2" stroke="currentColor" stroke-width="1.6" />
      </symbol>
    </defs>
  </svg>
</template>

<script setup>
// Component này không có logic, chỉ khai báo bộ icon SVG dạng <symbol>
// để các nơi khác (BlogCard.vue, BlogDetailView.vue) gọi qua <use href="#blog-icon-...">.
// Cách làm này giúp tránh import nhiều file .svg riêng lẻ, đồng thời icon kế thừa
// màu chữ hiện tại (currentColor) để khớp theme vàng-nâu của SUNOVA.
</script>