// src/constants/blogPosts.js
// Dữ liệu mẫu cho trang Blog của SUNOVA.
// Sau này bạn có thể thay thế file này bằng API thật (gọi qua src/api/blog.js)
// miễn là giữ đúng cấu trúc field bên dưới, các component sẽ không cần sửa gì thêm.

export const blogCategories = [
  { slug: 'tat-ca', label: 'Tất cả' },
  { slug: 'kien-thuc-spf', label: 'Kiến thức SPF' },
  { slug: 'thoi-tiet-da', label: 'Thời tiết & Da' },
  { slug: 'huong-dan-chon', label: 'Hướng dẫn chọn kem' },
  { slug: 'mua-mua', label: 'Mùa mưa' },
  { slug: 'cham-soc-da', label: 'Chăm sóc da' },
]

// icon: tên icon dùng trong BlogCard.vue (xem danh sách icon hỗ trợ trong component đó)
export const blogPosts = [
  {
    id: 1,
    slug: 'chi-so-uv-tai-viet-nam-khi-nao-can-thoa-lai-kem-chong-nang',
    title: 'Chỉ số UV tại Việt Nam: Khi nào cần thoa lại kem chống nắng?',
    excerpt:
      'Chỉ số UV ở Việt Nam thường ở mức rất cao từ tháng 4 đến tháng 9, kể cả khi trời nhiều mây. Đây là cách đọc chỉ số UV và lên lịch thoa lại kem chống nắng hợp lý theo từng khung giờ.',
    category: 'thoi-tiet-da',
    icon: 'sun',
    readTime: '6 phút đọc',
    publishDate: '2026-06-02',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Thời tiết',
    sections: [
      {
        heading: 'Chỉ số UV là gì và vì sao cần quan tâm',
        paragraphs: [
          'Chỉ số UV (UV Index) đo cường độ bức xạ tia cực tím chiếu xuống mặt đất tại một thời điểm, thang điểm phổ biến từ 0 đến 11+. Việt Nam nằm trong vùng nhiệt đới nên phần lớn thời gian trong năm, đặc biệt từ tháng 4 đến tháng 9, chỉ số UV tại các thành phố lớn thường dao động ở mức 8-11, được xếp vào nhóm rất cao đến cực cao.',
          'Ở mức này, da không được bảo vệ có thể bắt đầu tổn thương chỉ sau 10-15 phút tiếp xúc trực tiếp với ánh nắng vào khung giờ cao điểm.',
        ],
      },
      {
        heading: 'Khung giờ nào UV cao nhất trong ngày',
        paragraphs: [
          'Tia UV thường đạt đỉnh trong khoảng 10 giờ sáng đến 4 giờ chiều, khi mặt trời ở góc chiếu gần như vuông góc với mặt đất. Đây cũng là khung giờ cần thoa lại kem chống nắng thường xuyên hơn so với buổi sáng sớm hoặc chiều muộn.',
          'Một hiểu lầm phổ biến là trời mát hoặc có gió thì tia UV cũng yếu đi. Trên thực tế, nhiệt độ không khí và chỉ số UV là hai đại lượng độc lập — một ngày mùa đông se lạnh nhưng trời quang vẫn có thể có UV ở mức cao.',
        ],
      },
      {
        heading: 'Lịch thoa lại kem chống nắng gợi ý',
        paragraphs: [
          'Với hoạt động ngoài trời thông thường (đi lại, làm việc gần cửa sổ), nên thoa lại kem chống nắng mỗi 3-4 giờ. Nếu vận động mạnh, đổ nhiều mồ hôi hoặc tiếp xúc trực tiếp với nắng gắt giữa trưa, rút ngắn xuống còn 2 giờ một lần.',
          'Sau khi bơi hoặc lau mồ hôi bằng khăn, lớp kem chống nắng gần như đã bị loại bỏ một phần đáng kể dù sản phẩm có ghi chống nước — nên thoa lại ngay sau đó thay vì chờ đến mốc giờ cố định.',
        ],
      },
    ],
  },
  {
    id: 2,
    slug: 'mua-mua-van-can-chong-nang-su-that-ve-tia-uv-qua-may',
    title: 'Mùa mưa vẫn cần chống nắng? Sự thật về tia UV xuyên qua mây',
    excerpt:
      'Trời âm u hay mưa phùn khiến nhiều người bỏ qua bước chống nắng. Thực tế, mây mỏng chỉ cản được một phần nhỏ tia UV — và đây là lý do bạn vẫn nên duy trì thói quen này quanh năm.',
    category: 'mua-mua',
    icon: 'cloud-rain',
    readTime: '5 phút đọc',
    publishDate: '2026-05-18',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Mùa mưa',
    sections: [
      {
        heading: 'Mây có chặn được tia UV không',
        paragraphs: [
          'Tia UV có bước sóng ngắn và xuyên qua được lớp mây mỏng hoặc mây phân tán ở mức khá cao, ước tính 70-80% lượng UV vẫn lọt xuống mặt đất kể cả khi trời nhiều mây. Chỉ những cơn mưa dày, mây đen kịt mới làm giảm UV đáng kể, và ngay cả lúc đó chỉ số UV vẫn có thể ở mức trung bình.',
          'Ngoài ra, vào những ngày mưa giông xen kẽ nắng gắt — kiểu thời tiết rất quen thuộc ở miền Nam và miền Trung Việt Nam vào mùa mưa — khoảng thời gian hửng nắng giữa các cơn mưa thường có UV tăng vọt bất ngờ.',
        ],
      },
      {
        heading: 'Độ ẩm cao làm thay đổi cách kem chống nắng hoạt động',
        paragraphs: [
          'Mùa mưa đi kèm độ ẩm không khí cao, khiến da đổ dầu và đổ mồ hôi nhiều hơn dù không ra nắng trực tiếp. Lớp kem chống nắng vì vậy dễ bị trôi hoặc loang hơn bình thường, đặc biệt với các dòng kem chống nắng dạng kem đặc.',
          'Trong điều kiện này, kem chống nắng dạng gel hoặc dạng sữa mỏng nhẹ, có khả năng kiểm soát dầu thường giữ được lớp bảo vệ ổn định hơn so với kem chống nắng dạng đặc truyền thống.',
        ],
      },
      {
        heading: 'Vậy mùa mưa có nên giảm tần suất thoa kem',
        paragraphs: [
          'Không nên bỏ hẳn, nhưng có thể điều chỉnh: với những ngày mưa cả ngày, không ra ngoài, một lớp chống nắng vào buổi sáng kết hợp chỉ số SPF 30 trở lên cho sinh hoạt trong nhà gần cửa sổ là đủ. Với ngày mưa xen nắng, vẫn nên mang theo kem chống nắng để thoa lại sau mỗi lần tiếp xúc với nắng hửng.',
        ],
      },
    ],
  },
  {
    id: 3,
    slug: 'spf-pa-la-gi-giai-ma-chi-so-tren-vo-hop-kem-chong-nang',
    title: 'SPF, PA là gì? Giải mã các chỉ số trên vỏ hộp kem chống nắng',
    excerpt:
      'SPF 30, SPF 50+, PA+++ — những con số và ký hiệu này thực chất đang nói về điều gì? Bài viết giải thích ngắn gọn cách đọc nhãn kem chống nắng để chọn đúng sản phẩm cho nhu cầu của bạn.',
    category: 'kien-thuc-spf',
    icon: 'shield',
    readTime: '7 phút đọc',
    publishDate: '2026-04-22',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Kiến thức',
    sections: [
      {
        heading: 'SPF bảo vệ trước tia UVB',
        paragraphs: [
          'SPF (Sun Protection Factor) đo khả năng chống lại tia UVB — loại tia chính gây cháy nắng và sạm da. Con số đi kèm SPF không phải là thời gian "an toàn tuyệt đối" tính bằng phút, mà thể hiện mức độ kéo dài thời gian da có thể chịu được nắng trước khi bắt đầu đỏ rát, so với khi không thoa gì.',
          'SPF 30 ngăn được khoảng 97% tia UVB, SPF 50 ngăn khoảng 98%. Sự chênh lệch giữa hai mức này nhỏ hơn nhiều người tưởng, nên việc thoa đủ lượng và thoa lại đúng lúc quan trọng không kém việc chọn chỉ số SPF cao nhất.',
        ],
      },
      {
        heading: 'PA bảo vệ trước tia UVA',
        paragraphs: [
          'PA (Protection Grade of UVA) đo khả năng chống tia UVA — loại tia có bước sóng dài hơn, xuyên sâu vào da và là nguyên nhân chính gây lão hóa, nám, sạm da về lâu dài. Ký hiệu dấu cộng (+) thể hiện mức độ bảo vệ: PA+ là thấp, PA++++ là cao nhất hiện có trên thị trường.',
          'Một sản phẩm chống nắng "phổ rộng" (broad spectrum) cần có cả chỉ số SPF lẫn PA để bảo vệ toàn diện trước cả hai loại tia, vì UVB và UVA gây tổn thương theo những cơ chế khác nhau.',
        ],
      },
      {
        heading: 'Vậy nên chọn chỉ số nào',
        paragraphs: [
          'Với khí hậu nhiệt đới quanh năm nắng gắt như Việt Nam, mức khuyến nghị phổ biến cho sinh hoạt ngoài trời là SPF 30-50 kết hợp PA+++ trở lên. Với người làm việc ngoài trời nhiều giờ liên tục, có thể cân nhắc SPF 50+ PA++++ và ưu tiên thoa lại đều đặn hơn là chỉ dựa vào chỉ số ghi trên bao bì.',
        ],
      },
    ],
  },
  {
    id: 4,
    slug: 'chong-nang-vat-ly-vs-hoa-hoc-loai-nao-hop-khi-hau-nong-am',
    title: 'Chống nắng vật lý vs hóa học: Loại nào hợp với khí hậu nóng ẩm?',
    excerpt:
      'Hai cơ chế chống nắng khác nhau hoàn toàn về cách hoạt động trên da. Trong điều kiện nóng ẩm quanh năm, sự khác biệt này ảnh hưởng trực tiếp đến cảm giác sử dụng và hiệu quả thực tế.',
    category: 'huong-dan-chon',
    icon: 'droplet',
    readTime: '6 phút đọc',
    publishDate: '2026-03-30',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Hướng dẫn chọn',
    sections: [
      {
        heading: 'Cơ chế hoạt động khác nhau như thế nào',
        paragraphs: [
          'Kem chống nắng vật lý (khoáng chất) chứa các thành phần như Zinc Oxide hoặc Titanium Dioxide, tạo một lớp màng phản xạ tia UV ngay trên bề mặt da. Kem chống nắng hóa học chứa các hợp chất hữu cơ hấp thụ tia UV rồi chuyển hóa thành nhiệt lượng nhỏ giải phóng ra ngoài.',
          'Vì cơ chế phản xạ, chống nắng vật lý thường có hiệu lực ngay sau khi thoa, trong khi chống nắng hóa học thường cần khoảng 15-20 phút để các thành phần phát huy tác dụng đầy đủ.',
        ],
      },
      {
        heading: 'Cảm giác trên da trong thời tiết nóng ẩm',
        paragraphs: [
          'Chống nắng vật lý truyền thống dễ để lại tông trắng và cảm giác dày, bí da khi kết hợp với mồ hôi — đây là lý do nhiều người ngại dùng vào mùa hè. Các công thức vật lý thế hệ mới dùng hạt khoáng siêu mịn đã cải thiện đáng kể vấn đề này.',
          'Chống nắng hóa học thường có kết cấu mỏng nhẹ, thấm nhanh, ít để lại vệt trắng, phù hợp với khí hậu nóng ẩm và những ai cần trang điểm ngay sau đó. Đổi lại, một số thành phần hóa học có thể gây châm chích nhẹ trên da nhạy cảm hoặc da đang tổn thương.',
        ],
      },
      {
        heading: 'Gợi ý lựa chọn theo nhu cầu',
        paragraphs: [
          'Da nhạy cảm, da đang mụn viêm hoặc dùng cho trẻ em: ưu tiên chống nắng vật lý hoặc dạng lai (hybrid) có tỷ lệ khoáng chất cao. Da dầu, hoạt động ngoài trời nhiều, cần cảm giác mỏng nhẹ: chống nắng hóa học hoặc dạng lai thiên về hóa học thường dễ chịu hơn trong điều kiện nóng ẩm kéo dài.',
        ],
      },
    ],
  },
  {
    id: 5,
    slug: 'do-am-cao-do-mo-hoi-nhieu-bi-quyet-giu-lop-chong-nang-ben-ca-ngay',
    title: 'Độ ẩm cao, đổ mồ hôi nhiều: Bí quyết giữ lớp chống nắng bền cả ngày',
    excerpt:
      'Khí hậu nóng ẩm khiến lớp chống nắng dễ trôi theo mồ hôi và dầu thừa chỉ sau vài giờ. Một vài điều chỉnh nhỏ trong cách thoa và chọn sản phẩm có thể giúp lớp bảo vệ bền hơn đáng kể.',
    category: 'thoi-tiet-da',
    icon: 'thermometer',
    readTime: '5 phút đọc',
    publishDate: '2026-06-20',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Thời tiết',
    sections: [
      {
        heading: 'Vì sao mồ hôi và dầu thừa làm giảm hiệu quả chống nắng',
        paragraphs: [
          'Khi đổ mồ hôi, lớp kem chống nắng trên da bị pha loãng và dễ bị lau trôi một phần khi bạn dùng khăn giấy hoặc tay quệt mặt. Dầu thừa tiết ra trong thời tiết nóng cũng có thể làm thay đổi kết cấu lớp kem, khiến nó vón cục hoặc trôi theo từng mảng thay vì đều khắp bề mặt da.',
        ],
      },
      {
        heading: 'Thoa đúng lượng ngay từ đầu',
        paragraphs: [
          'Một trong những nguyên nhân phổ biến khiến chống nắng "không có tác dụng" thực chất là do thoa quá ít. Lượng khuyến nghị cho vùng mặt và cổ là khoảng 1/4 thìa cà phê (tương đương kích thước đồng xu), thoa thành nhiều lớp mỏng thay vì một lớp dày.',
        ],
      },
      {
        heading: 'Mẹo giữ lớp chống nắng bền lâu trong ngày nóng',
        paragraphs: [
          'Dùng giấy thấm dầu nhẹ nhàng trước khi thoa lại kem, tránh chà xát mạnh làm bong lớp chống nắng cũ. Cân nhắc dạng xịt hoặc dạng phấn chống nắng để thoa lại nhanh mà không làm trôi lớp trang điểm bên dưới.',
          'Kết hợp thêm các vật lý chắn nắng như mũ rộng vành, áo chống nắng hoặc tìm bóng râm trong khung giờ UV cao điểm — đây vẫn là lớp bảo vệ bổ sung hiệu quả, không thể thay thế hoàn toàn bằng kem chống nắng nhưng giúp giảm tần suất phải thoa lại.',
        ],
      },
    ],
  },
  {
    id: 6,
    slug: 'lich-thoa-kem-chong-nang-theo-mua-o-ba-mien',
    title: 'Lịch thoa kem chống nắng theo mùa ở ba miền Việt Nam',
    excerpt:
      'Bắc, Trung, Nam có kiểu thời tiết khác nhau rõ rệt theo mùa. Đây là gợi ý điều chỉnh thói quen chống nắng phù hợp với đặc điểm khí hậu từng vùng trong năm.',
    category: 'cham-soc-da',
    icon: 'map',
    readTime: '8 phút đọc',
    publishDate: '2026-01-15',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Chăm sóc da',
    sections: [
      {
        heading: 'Miền Bắc: mùa đông không có nghĩa là an toàn',
        paragraphs: [
          'Miền Bắc có mùa đông se lạnh, nhiều ngày trời hanh khô và quang mây — đây lại là điều kiện khiến tia UV phản xạ mạnh, đặc biệt khi trời nắng hanh. Mùa hè miền Bắc nắng gắt kèm độ ẩm cao, cần ưu tiên SPF cao và kết cấu kiểm soát dầu.',
          'Mùa hanh khô cũng khiến da dễ mất nước hơn, nên kết hợp kem chống nắng với bước dưỡng ẩm đầy đủ để tránh tình trạng bong tróc làm giảm hiệu quả của lớp chống nắng.',
        ],
      },
      {
        heading: 'Miền Trung: nắng gắt kéo dài và gió biển',
        paragraphs: [
          'Miền Trung có mùa nắng nóng kéo dài với cường độ UV thuộc nhóm cao nhất cả nước, đặc biệt giữa trưa. Gió biển và độ mặn trong không khí ở vùng ven biển cũng có thể khiến da nhạy cảm hơn, dễ kích ứng khi kết hợp với ánh nắng gắt.',
          'Với đặc điểm này, nên ưu tiên SPF 50+ PA++++ vào mùa hè, thoa lại thường xuyên hơn khi hoạt động ngoài trời hoặc gần biển.',
        ],
      },
      {
        heading: 'Miền Nam: nắng và mưa xen kẽ quanh năm',
        paragraphs: [
          'Miền Nam có khí hậu hai mùa rõ rệt: mùa khô nắng gắt liên tục và mùa mưa với những cơn mưa rào xen nắng. Đặc điểm "nắng mưa thất thường" khiến việc chủ động mang theo kem chống nắng để thoa lại quan trọng hơn là chỉ dựa vào một lần thoa buổi sáng.',
          'Độ ẩm cao quanh năm ở miền Nam cũng là lý do các dạng kem chống nắng mỏng nhẹ, thấm nhanh thường được ưa chuộng hơn dạng kem đặc truyền thống.',
        ],
      },
    ],
  },
]

export function getPostBySlug(slug) {
  return blogPosts.find((post) => post.slug === slug) || null
}

export function getRelatedPosts(currentPost, limit = 3) {
  if (!currentPost) return []
  return blogPosts
    .filter((post) => post.id !== currentPost.id && post.category === currentPost.category)
    .slice(0, limit)
    .concat(
      blogPosts
        .filter((post) => post.id !== currentPost.id && post.category !== currentPost.category)
        .slice(0, Math.max(0, limit - 1)),
    )
    .slice(0, limit)
}