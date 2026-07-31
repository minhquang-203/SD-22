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
  {
    id: 7,
    slug: 'cach-chon-kem-chong-nang-cho-da-dau-mun',
    title: 'Cách chọn kem chống nắng cho da dầu mụn',
    excerpt:
      'Da dầu mụn cần lớp chống nắng mỏng nhẹ, ít gây bí tắc nhưng vẫn đủ SPF và PA. Đây là tiêu chí chọn sản phẩm và cách thoa để hạn chế bóng nhờn, mụn viêm trong khí hậu nóng ẩm.',
    category: 'huong-dan-chon',
    icon: 'droplet',
    readTime: '7 phút đọc',
    publishDate: '2026-07-05',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Hướng dẫn chọn',
    sections: [
      {
        heading: 'Vì sao da dầu mụn dễ “kỵ” kem chống nắng',
        paragraphs: [
          'Da dầu có tuyến bã hoạt động mạnh, đặc biệt ở vùng chữ T. Khi thêm một lớp kem chống nắng đặc hoặc nhiều dầu, lỗ chân lông dễ bị bịt kín, cộng thêm mồ hôi và bụi bẩn trong ngày khiến tình trạng mụn viêm, mụn đầu đen dễ nặng hơn.',
          'Điều đó không có nghĩa là bỏ chống nắng. Tia UV vẫn gây sạm, thâm sau mụn và làm da dễ kích ứng hơn về lâu dài. Mục tiêu là chọn kết cấu phù hợp và thoa đúng lượng thay vì bỏ bước bảo vệ.',
        ],
      },
      {
        heading: 'Ưu tiên kết cấu và chỉ số nào',
        paragraphs: [
          'Với da dầu mụn, các dạng gel, sữa mỏng hoặc kem chống nắng “oil control / matte” thường dễ chịu hơn dạng kem đặc. Nhãn ghi không chứa dầu (oil-free), không gây mụn (non-comedogenic) là tín hiệu hữu ích, dù vẫn cần thử trên da thật để xác nhận.',
          'Về chỉ số, SPF 30–50 kèm PA+++ trở lên vẫn là mức phù hợp cho sinh hoạt ngoài trời ở Việt Nam. Không nhất thiết phải chọn SPF cao nhất nếu sản phẩm quá đặc và khiến bạn ngại thoa lại — sự đều đặn quan trọng hơn con số trên bao bì.',
        ],
      },
      {
        heading: 'Thứ tự chăm sóc và lượng thoa hợp lý',
        paragraphs: [
          'Sau bước làm sạch và dưỡng ẩm nhẹ (ưu tiên gel hoặc lotion không dầu), thoa kem chống nắng như lớp cuối cùng buổi sáng trước trang điểm. Lượng gợi ý cho mặt khoảng hai đốt ngón tay — thoa quá mỏng sẽ làm giảm hiệu quả bảo vệ dù SPF ghi trên hộp rất cao.',
          'Nếu da dễ bóng giữa ngày, có thể thấm dầu bằng giấy thấm trước rồi thoa lại lớp mỏng vùng chữ T, hoặc dùng sản phẩm chống nắng dạng xịt/phấn bổ sung ngoài trời. Tránh chồng nhiều lớp kem đặc liên tục vì dễ gây bí và bong cục.',
        ],
      },
      {
        heading: 'Khi nào nên đổi sản phẩm hoặc gặp bác sĩ da liễu',
        paragraphs: [
          'Nếu sau 1–2 tuần dùng vẫn nổi nhiều mụn viêm mới, ngứa hoặc đỏ kéo dài, hãy dừng sản phẩm và thử lại với kết cấu mỏng hơn hoặc công thức dành riêng cho da mụn. Trường hợp mụn đang điều trị bằng thuốc bôi/uống, nên hỏi bác sĩ trước khi đổi kem chống nắng để tránh kích ứng chồng chéo.',
        ],
      },
    ],
  },
  {
    id: 8,
    slug: 'chong-nang-cho-da-khi-di-mua-va-loi-nuoc',
    title: 'Chống nắng cho da khi đi mưa và lội nước',
    excerpt:
      'Mưa và nước làm lớp kem chống nắng mỏng đi nhanh hơn bạn nghĩ. Bài viết giải thích vì sao vẫn cần chống nắng ngày mưa, và cách duy trì bảo vệ khi ướt áo, lội nước hoặc đi dưới mưa phùn.',
    category: 'mua-mua',
    icon: 'cloud-rain',
    readTime: '6 phút đọc',
    publishDate: '2026-07-12',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Mùa mưa',
    sections: [
      {
        heading: 'Đi mưa vẫn có tia UV',
        paragraphs: [
          'Mây và mưa làm giảm một phần ánh sáng nhìn thấy, nhưng tia UV vẫn có thể xuyên xuống mặt đất ở mức đáng kể, nhất là khi trời sáng dịu hoặc mưa phùn mỏng. Khoảng thời gian hửng nắng giữa các cơn mưa còn khiến chỉ số UV tăng nhanh trong thời gian ngắn.',
          'Vì vậy, bỏ hẳn kem chống nắng chỉ vì trời âm u là thói quen dễ khiến da sạm và cháy nắng nhẹ sau những ngày mưa xen nắng quen thuộc ở nhiều tỉnh thành Việt Nam.',
        ],
      },
      {
        heading: 'Nước và mồ hôi làm lớp bảo vệ mỏng nhanh',
        paragraphs: [
          'Kem chống nắng “chống nước” không có nghĩa là không bị trôi. Khi da ướt, lau khăn hoặc lội nước, một phần màng chống nắng bị loại bỏ. Nhà sản xuất thường khuyến nghị thoa lại sau khi ra khỏi nước hoặc sau khoảng thời gian tiếp xúc nước liên tục.',
          'Áo mưa và mũ giúp che mưa nhưng không thay thế hoàn toàn lớp chống nắng trên vùng da hở như mặt, cổ, tai và mu bàn tay — những vị trí vẫn tiếp xúc ánh sáng và phản xạ từ mặt đường ướt.',
        ],
      },
      {
        heading: 'Gợi ý thực tế cho ngày mưa phải ra ngoài',
        paragraphs: [
          'Buổi sáng vẫn nên thoa đủ lượng kem chống nắng phổ rộng trước khi ra đường. Chọn kết cấu bền nước vừa phải nhưng không quá đặc nếu độ ẩm cao khiến da dễ đổ dầu. Mang theo tuýp nhỏ để thoa lại sau khi áo mưa ướt, lau mặt hoặc ngồi lâu gần cửa sổ xe/bus.',
          'Nếu phải lội nước hoặc đi dưới mưa lâu, ưu tiên thoa lại vùng mặt và cổ ngay khi có điều kiện lau khô da. Kết hợp thêm phụ kiện che nắng (mũ rộng vành, áo dài tay mỏng) sẽ giảm tải cho lớp kem và giúp da dễ chịu hơn trong ngày ẩm ướt.',
        ],
      },
    ],
  },
  {
    id: 9,
    slug: 'tre-em-va-chong-nang-do-tuoi-nao-bat-dau',
    title: 'Trẻ em và chống nắng: độ tuổi nào bắt đầu',
    excerpt:
      'Da trẻ mỏng và nhạy cảm hơn người lớn, nhưng cách bảo vệ cần phù hợp từng độ tuổi. Bài viết tóm tắt nguyên tắc che nắng, dùng kem và thói quen an toàn theo từng giai đoạn phát triển.',
    category: 'cham-soc-da',
    icon: 'sun',
    readTime: '7 phút đọc',
    publishDate: '2026-07-18',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Chăm sóc da',
    sections: [
      {
        heading: 'Trẻ sơ sinh và giai dưới 6 tháng',
        paragraphs: [
          'Với trẻ rất nhỏ, ưu tiên hàng đầu là tránh nắng trực tiếp: ở trong bóng râm, dùng mũ, áo dài tay mỏng và hạn chế ra ngoài vào khung giờ UV cao. Nhiều hướng dẫn da liễu khuyến cáo hạn chế dùng kem chống nắng hóa học trên trẻ dưới 6 tháng trừ khi không còn cách che chắn khác và có chỉ định của bác sĩ.',
          'Nếu buộc phải ra ngoài ngắn, hãy ưu tiên bóng râm và trang phục che phủ thay vì dựa vào kem chống nắng như người lớn.',
        ],
      },
      {
        heading: 'Từ khoảng 6 tháng tuổi trở đi',
        paragraphs: [
          'Khi trẻ lớn hơn, có thể bắt đầu dùng kem chống nắng dành riêng cho trẻ em, thường ưu tiên công thức vật lý (khoáng) dịu da và ít hương liệu. Vẫn nên kết hợp mũ, kính (nếu phù hợp), áo chống nắng và tránh phơi nắng giữa trưa.',
          'Thoa thử một lượng nhỏ ở vùng da kín trước lần dùng đầu để theo dõi kích ứng. Thoa đủ lượng vùng da hở và thoa lại sau khi chơi nước, đổ mồ hôi nhiều hoặc lau khăn.',
        ],
      },
      {
        heading: 'Trẻ lớn và học sinh: xây thói quen bền vững',
        paragraphs: [
          'Ở độ tuổi đi học, ngoài kem chống nắng buổi sáng, phụ huynh nên nhắc trẻ đội mũ khi ra sân và thoa lại sau giờ thể dục nếu da ướt mồ hôi. Chọn sản phẩm dễ thoa, ít bết dính sẽ giúp trẻ hợp tác hơn thay vì dùng loại quá đặc.',
          'Không dùng chung kem chống nắng người lớn có nồng độ hoạt chất cao hoặc nhiều thành phần dễ kích ứng nếu chưa phù hợp da trẻ. Khi trẻ có bệnh da, dị ứng hoặc đang điều trị, hãy hỏi bác sĩ trước khi chọn sản phẩm mới.',
        ],
      },
      {
        heading: 'Những dấu hiệu cần dừng và đi khám',
        paragraphs: [
          'Nếu sau khi thoa kem xuất hiện mẩn đỏ, ngứa, sưng hoặc nổi mụn nước, hãy rửa sạch vùng da, dừng sản phẩm và đưa trẻ đi khám nếu triệu chứng kéo dài. Cháy nắng ở trẻ cũng cần được xử lý sớm: vào bóng râm, làm mát da và tránh nắng thêm cho đến khi da hồi phục.',
        ],
      },
    ],
  },
  {
    id: 10,
    slug: 'sai-lam-thuong-gap-khi-boi-kem-chong-nang',
    title: 'Sai lầm thường gặp khi bôi kem chống nắng',
    excerpt:
      'Chọn SPF cao chưa đủ nếu thoa sai cách. Dưới đây là những lỗi phổ biến khiến lớp bảo vệ kém hiệu quả — và cách chỉnh lại thói quen hàng ngày cho khí hậu Việt Nam.',
    category: 'kien-thuc-spf',
    icon: 'shield',
    readTime: '6 phút đọc',
    publishDate: '2026-07-25',
    author: 'Đội ngũ biên tập SUNOVA',
    tag: 'Kiến thức',
    sections: [
      {
        heading: 'Thoa quá mỏng và bỏ sót vùng da',
        paragraphs: [
          'Nhiều người chỉ chấm vài điểm kem rồi xoa nhanh, lượng thực tế thấp hơn nhiều so với mức dùng khi đo SPF trong điều kiện chuẩn. Hệ quả là bảo vệ yếu hơn đáng kể dù hộp ghi SPF 50.',
          'Nên thoa đều mặt, tai, cổ, gáy và mu bàn tay — những vùng dễ cháy nắng nhưng thường bị quên. Với môi và mí mắt, dùng sản phẩm phù hợp hoặc son/kem có SPF nếu da vùng đó nhạy cảm.',
        ],
      },
      {
        heading: 'Chỉ thoa một lần buổi sáng rồi quên cả ngày',
        paragraphs: [
          'Kem chống nắng bị mồ hôi, ma sát khẩu trang, lau mặt và ánh nắng làm mỏng dần theo thời gian. Một lần thoa lúc 7 giờ sáng thường không đủ cho cả buổi chiều ngoài trời.',
          'Hãy đặt mốc thoa lại theo hoạt động: khoảng vài giờ một lần khi ở ngoài trời, và ngay sau khi đổ nhiều mồ hôi, bơi hoặc lau khăn. Trong văn phòng gần cửa sổ lớn, vẫn nên duy trì lớp buổi sáng và cân nhắc thoa lại nếu ngồi cạnh kính nhiều giờ.',
        ],
      },
      {
        heading: 'Nhầm lẫn SPF cao với “không cần thoa lại”',
        paragraphs: [
          'SPF cao hơn không đồng nghĩa với thời gian bảo vệ vô hạn. Nó chủ yếu nói lên mức lọc tia UVB khi thoa đủ lượng; hiệu quả vẫn phụ thuộc vào việc duy trì lớp kem trên da.',
          'Tương tự, trời mát, ngồi trong xe hoặc đứng dưới mái hiên không có nghĩa là UV bằng không. Kính xe và bóng râm chỉ giảm một phần bức xạ — vùng da hở vẫn nên được bảo vệ.',
        ],
      },
      {
        heading: 'Bôi kem chống nắng lên da chưa ổn định',
        paragraphs: [
          'Thoa kem chống nắng ngay trên da đang kích ứng mạnh, vừa peel hóa chất, hoặc chưa chờ dưỡng ẩm thấm có thể gây bóng cục, không đều và dễ khó chịu. Nên chờ lớp dưỡng thẩm thấu vài phút rồi mới thoa chống nắng.',
          'Cuối ngày, hãy làm sạch nhẹ nhàng để loại bỏ kem chống nắng và bụi bẩn; để lớp kem qua đêm dễ bí da và không giúp “chống nắng thêm” khi đã không còn tiếp xúc nắng.',
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