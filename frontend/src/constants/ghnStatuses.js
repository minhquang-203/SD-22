/** Danh sách trạng thái vận đơn GHN dùng cho giả lập webhook (test). */
export const GHN_STATUS_OPTIONS = [
  { value: 'ready_to_pick', label: 'Chờ lấy hàng' },
  { value: 'picking', label: 'Đang lấy hàng' },
  { value: 'cancel', label: 'Đã hủy giao hàng' },
  { value: 'money_collect_picking', label: 'Đang thu tiền khi lấy hàng' },
  { value: 'picked', label: 'Đã lấy hàng' },
  { value: 'storing', label: 'Đang nhập kho' },
  { value: 'transporting', label: 'Đang vận chuyển' },
  { value: 'sorting', label: 'Đang phân loại' },
  { value: 'delivering', label: 'Đang giao hàng' },
  { value: 'money_collect_delivering', label: 'Đang thu tiền khi giao hàng' },
  { value: 'delivered', label: 'Đã giao' },
  { value: 'delivery_fail', label: 'Giao hàng không thành công' },
  { value: 'waiting_to_return', label: 'Chờ hoàn hàng' },
  { value: 'return', label: 'Đang hoàn hàng' },
  { value: 'return_transporting', label: 'Đang chuyển hoàn' },
  { value: 'return_sorting', label: 'Đang phân loại hoàn' },
  { value: 'returning', label: 'Shipper đang hoàn' },
  { value: 'return_fail', label: 'Hoàn hàng không thành công' },
  // "returned" / Đã hoàn hàng: không cho NV/admin giả lập — chỉ từ đồng bộ GHN thật.
  { value: 'exception', label: 'Có sự cố vận chuyển' },
  { value: 'damage', label: 'Hàng hư hỏng' },
  { value: 'lost', label: 'Hàng thất lạc' },
]

/**
 * Trạng thái GHN "chính" dùng cho màn cập nhật trạng thái giao hàng nhanh của admin.
 *
 * Chỉ gồm các mốc ánh xạ trực tiếp sang trạng thái vòng đời của dự án
 * (Đang chuẩn bị → Đang giao → Hoàn thành). KHÔNG bao gồm các trạng thái
 * hoàn/trả hàng, hủy hay sự cố — trả hàng phải đi qua luồng yêu cầu trả hàng,
 * không cho admin tự chuyển ở màn này.
 */
export const GHN_STATUS_MAIN_OPTIONS = [
  { value: 'ready_to_pick', label: 'Chờ lấy hàng' }, // -> Đang chuẩn bị
  { value: 'picked', label: 'Đã lấy hàng' }, // -> Đang giao
  { value: 'delivering', label: 'Đang giao hàng' }, // -> Đang giao
  { value: 'delivered', label: 'Đã giao' }, // -> Hoàn thành
]
