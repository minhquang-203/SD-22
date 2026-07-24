/** Nhóm sidebar + nội dung trang thông tin storefront */

export const INFO_GROUPS = [
  {
    id: 'policy',
    label: 'Chính sách',
    links: [
      { path: '/chinh-sach-chung', title: 'Chính sách chung' },
      { path: '/thanh-toan', title: 'Thanh toán' },
      { path: '/doi-tra-hoan-tien', title: 'Đổi trả & hoàn tiền' },
      { path: '/van-chuyen', title: 'Vận chuyển' },
      { path: '/bao-mat', title: 'Bảo mật' },
    ],
  },
  {
    id: 'about',
    label: 'Về SUNOVA',
    links: [
      { path: '/gioi-thieu', title: 'Giới thiệu' },
      { path: '/lien-he', title: 'Liên hệ' },
      { path: '/he-thong-cua-hang', title: 'Hệ thống cửa hàng' },
    ],
  },
]

export const CONTACT_FAKE = {
  hotline: '1900 6868',
  email: 'cskh@sunova.vn',
  address: 'Tầng 5, 80 Phố Chùa Bộc, Đống Đa, Hà Nội',
  hours: 'Thứ 2 – Thứ 7: 8:00 – 21:00 · Chủ nhật: 9:00 – 18:00',
}

export const STORES_FAKE = [
  {
    city: 'Hà Nội',
    name: 'SUNOVA Chùa Bộc',
    address: 'Tầng 1, 80 Phố Chùa Bộc, Đống Đa, Hà Nội',
    phone: '024 3856 6868',
    hours: '9:00 – 21:00',
  },
  {
    city: 'TP. Hồ Chí Minh',
    name: 'SUNOVA Nguyễn Huệ',
    address: '45 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
    phone: '028 3822 6868',
    hours: '9:00 – 22:00',
  },
  {
    city: 'Đà Nẵng',
    name: 'SUNOVA Bạch Đằng',
    address: '12 Bạch Đằng, Hải Châu, Đà Nẵng',
    phone: '0236 388 6868',
    hours: '9:00 – 21:00',
  },
  {
    city: 'Hà Nội',
    name: 'SUNOVA Royal City',
    address: 'Tầng B2, Royal City, 72A Nguyễn Trãi, Thanh Xuân, Hà Nội',
    phone: '024 6666 6868',
    hours: '10:00 – 22:00',
  },
]

export const INFO_PAGES = {
  'gioi-thieu': {
    title: 'Giới thiệu',
    groupId: 'about',
    metaTitle: 'Giới thiệu — SUNOVA',
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'SUNOVA là thương hiệu và chuỗi bán lẻ chuyên kem chống nắng cùng giải pháp chăm sóc da, được xây dựng cho khí hậu nhiệt đới và làn da người Việt. Chúng tôi tuyển chọn sản phẩm từ các thương hiệu uy tín trong và ngoài nước, cam kết 100% chính hãng, nguồn gốc minh bạch.',
          'Sứ mệnh của SUNOVA là "bảo vệ làn da dưới ánh nắng Việt" — mang đến trải nghiệm chống nắng tinh tế, kết cấu nhẹ, không bí da, đồng thời đồng hành cùng khách hàng trong hành trình chăm sóc da khoa học mỗi ngày.',
        ],
      },
      {
        type: 'heading',
        text: 'Giá trị cốt lõi',
      },
      {
        type: 'list',
        items: [
          'Chính hãng — mỗi sản phẩm có nguồn gốc rõ ràng, tem phân phối đầy đủ.',
          'Tư vấn tận tâm — đội ngũ am hiểu SPF, PA và loại da để gợi ý phù hợp.',
          'Trải nghiệm Soleil — thẩm mỹ sang trọng, dịch vụ giao hàng nhanh và đổi trả linh hoạt.',
        ],
      },
    ],
  },
  'chinh-sach-chung': {
    title: 'Chính sách chung',
    groupId: 'policy',
    metaTitle: 'Chính sách chung — SUNOVA',
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'Khi mua sắm tại SUNOVA (website sunova.vn và hệ thống cửa hàng), khách hàng được bảo đảm quyền lợi theo quy định pháp luật Việt Nam về bảo vệ người tiêu dùng và thương mại điện tử.',
        ],
      },
      {
        type: 'heading',
        text: 'Nguyên tắc mua bán',
      },
      {
        type: 'list',
        items: [
          'Giá niêm yết trên website là giá bán lẻ đã bao gồm VAT (trừ khi có ghi chú khác).',
          'Đơn hàng chỉ được xác nhận sau khi SUNOVA gửi email/SMS xác nhận hoặc liên hệ thành công.',
          'SUNOVA có quyền từ chối đơn hàng nếu phát hiện gian lận, thông tin sai lệch hoặc hết hàng.',
        ],
      },
      {
        type: 'heading',
        text: 'Quyền lợi khách hàng',
      },
      {
        type: 'list',
        items: [
          'Được cung cấp đầy đủ thông tin sản phẩm, hóa đơn và chứng từ khi yêu cầu.',
          'Đổi trả trong 7 ngày theo chính sách đổi trả & hoàn tiền.',
        ],
      },
    ],
  },
  'thanh-toan': {
    title: 'Thanh toán',
    groupId: 'policy',
    metaTitle: 'Thanh toán — SUNOVA',
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'SUNOVA hỗ trợ nhiều hình thức thanh toán an toàn, thuận tiện cho khách hàng mua online và tại cửa hàng.',
        ],
      },
      {
        type: 'heading',
        text: 'Hình thức thanh toán',
      },
      {
        type: 'list',
        items: [
          'Thanh toán khi nhận hàng (COD).',
          'Chuyển khoản ngân hàng — thông tin tài khoản được gửi kèm email xác nhận đơn.',
          'Ví điện tử: MoMo, ZaloPay.',
          'Thẻ ATM nội địa / thẻ tín dụng quốc tế (Visa, Mastercard) qua cổng thanh toán đối tác.',
        ],
      },
      {
        type: 'text',
        paragraphs: [
          'Đơn hàng chuyển khoản sẽ được xử lý sau khi SUNOVA xác nhận đã nhận đủ tiền. Vui lòng ghi rõ mã đơn hàng khi chuyển khoản.',
        ],
      },
    ],
  },
  'doi-tra-hoan-tien': {
    title: 'Đổi trả & hoàn tiền',
    groupId: 'policy',
    metaTitle: 'Đổi trả & hoàn tiền — SUNOVA',
    blocks: [
      {
        type: 'heading',
        text: 'Điều kiện đổi trả',
      },
      {
        type: 'list',
        items: [
          'Sản phẩm còn nguyên tem, seal, chưa qua sử dụng, đầy đủ hộp và phụ kiện kèm theo.',
          'Yêu cầu đổi trả trong vòng 7 ngày kể từ khi nhận hàng.',
          'Có hóa đơn mua hàng hoặc email xác nhận đơn từ SUNOVA.',
        ],
      },
      {
        type: 'heading',
        text: 'Quy trình',
      },
      {
        type: 'list',
        items: [
          'Liên hệ hotline 1900 6868 hoặc email cskh@sunova.vn, cung cấp mã đơn và lý do đổi trả.',
          'SUNOVA hướng dẫn gửi hàng về kho hoặc mang sản phẩm đến cửa hàng gần nhất.',
          'Sau khi kiểm tra, chúng tôi đổi sản phẩm tương đương hoặc hoàn tiền theo phương thức thanh toán ban đầu.',
        ],
      },
      {
        type: 'text',
        paragraphs: [
          'Thời gian hoàn tiền: 3–7 ngày làm việc đối với chuyển khoản/ví điện tử; 1–2 chu kỳ sao kê đối với thẻ tín dụng.',
        ],
      },
    ],
  },
  'van-chuyen': {
    title: 'Vận chuyển',
    groupId: 'policy',
    metaTitle: 'Vận chuyển — SUNOVA',
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'SUNOVA giao hàng toàn quốc thông qua đối tác GHN, GHTK và các đơn vị vận chuyển uy tín khác.',
        ],
      },
      {
        type: 'heading',
        text: 'Phí và thời gian',
      },
      {
        type: 'list',
        items: [
          'Thời gian giao hàng dự kiến: 2–5 ngày làm việc (nội thành thường 1–3 ngày).',
          'Phí vận chuyển được tính theo trọng lượng và địa chỉ nhận, hiển thị trước khi đặt hàng.',
        ],
      },
    ],
  },
  'bao-mat': {
    title: 'Bảo mật',
    groupId: 'policy',
    metaTitle: 'Bảo mật — SUNOVA',
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'SUNOVA cam kết bảo vệ thông tin cá nhân của khách hàng theo quy định pháp luật hiện hành.',
        ],
      },
      {
        type: 'heading',
        text: 'Thu thập & sử dụng',
      },
      {
        type: 'list',
        items: [
          'Chúng tôi thu thập họ tên, số điện thoại, email, địa chỉ giao hàng để xử lý đơn hàng và chăm sóc khách hàng.',
          'Thông tin thanh toán được mã hóa qua cổng thanh toán; SUNOVA không lưu trữ số thẻ đầy đủ.',
          'Dữ liệu có thể được dùng để gửi thông tin khuyến mãi nếu khách hàng đồng ý nhận bản tin.',
        ],
      },
      {
        type: 'heading',
        text: 'Cam kết',
      },
      {
        type: 'list',
        items: [
          'Không bán hoặc chia sẻ thông tin cá nhân cho bên thứ ba ngoài mục đích vận hành đơn hàng.',
          'Khách hàng có quyền yêu cầu chỉnh sửa hoặc xóa dữ liệu bằng cách liên hệ cskh@sunova.vn.',
        ],
      },
    ],
  },
  'lien-he': {
    title: 'Liên hệ',
    groupId: 'about',
    metaTitle: 'Liên hệ — SUNOVA',
    showContactForm: true,
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'Đội ngũ SUNOVA luôn sẵn sàng hỗ trợ bạn về đơn hàng, sản phẩm và chính sách. Vui lòng liên hệ qua các kênh dưới đây hoặc gửi biểu mẫu.',
        ],
      },
    ],
  },
  'he-thong-cua-hang': {
    title: 'Hệ thống cửa hàng',
    groupId: 'about',
    metaTitle: 'Hệ thống cửa hàng — SUNOVA',
    showStores: true,
    blocks: [
      {
        type: 'text',
        paragraphs: [
          'Ghé thăm showroom SUNOVA để trải nghiệm kết cấu sản phẩm và được tư vấn chọn kem chống nắng phù hợp.',
        ],
      },
    ],
  },
}

export function getInfoPageByPath(path) {
  const slug = path.replace(/^\//, '')
  return INFO_PAGES[slug] || null
}

export function getGroupForPage(page) {
  if (!page) return null
  return INFO_GROUPS.find((g) => g.id === page.groupId)
}
