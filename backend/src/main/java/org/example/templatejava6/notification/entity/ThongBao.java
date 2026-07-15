package org.example.templatejava6.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.templatejava6.notification.enums.LoaiThongBao;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

/**
 * Map vào bảng thong_bao đã có sẵn trong V1__init.sql.
 * Các cột bổ sung (link, id_tham_chieu, ma_tham_chieu) được thêm ở V11.
 * Thông báo dành cho admin/hệ thống có id_khach_hang = NULL.
 */
@Getter
@Setter
@Entity
@Table(name = "thong_bao")
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", length = 30)
    private LoaiThongBao loai;

    @Nationalized
    @Column(name = "tieu_de", length = 150, nullable = false)
    private String tieuDe;

    @Nationalized
    @Column(name = "noi_dung", length = 500)
    private String noiDung;

    /** NULL = thông báo cho admin/hệ thống (không gắn với 1 khách cụ thể). */
    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    /** Route phía admin để điều hướng khi bấm vào thông báo. */
    @Column(name = "link", length = 255)
    private String link;

    /** Id đối tượng liên quan (vd: id hóa đơn). */
    @Column(name = "id_tham_chieu")
    private Integer idThamChieu;

    /** Mã đối tượng liên quan (vd: mã hóa đơn) để hiển thị nhanh. */
    @Column(name = "ma_tham_chieu", length = 30)
    private String maThamChieu;

    @ColumnDefault("0")
    @Column(name = "da_doc")
    private Boolean daDoc = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;
}
