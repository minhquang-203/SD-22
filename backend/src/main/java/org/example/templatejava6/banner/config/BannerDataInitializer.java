package org.example.templatejava6.banner.config;

import org.example.templatejava6.banner.entity.BannerTrangChu;
import org.example.templatejava6.banner.repository.BannerTrangChuRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tạo banner quiz mặc định một lần khi bảng còn trống.
 * Banner đã có (kể cả bản admin đã sửa) không bị ghi đè khi khởi động lại.
 */
@Component
public class BannerDataInitializer implements ApplicationRunner {

    private final BannerTrangChuRepository bannerTrangChuRepository;

    public BannerDataInitializer(BannerTrangChuRepository bannerTrangChuRepository) {
        this.bannerTrangChuRepository = bannerTrangChuRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Chỉ tạo khi chưa có banner. Không ghi đè banner đã sửa trong admin
        // (kể cả banner quiz mặc định) mỗi lần khởi động.
        if (bannerTrangChuRepository.count() > 0) {
            return;
        }

        BannerTrangChu banner = new BannerTrangChu();
        applyDefaultQuizContent(banner);
        banner.setThuTu(1);
        banner.setTrangThai(true);
        bannerTrangChuRepository.save(banner);
    }

    private void applyDefaultQuizContent(BannerTrangChu banner) {
        banner.setTieuDe("Trắc nghiệm da");
        banner.setTieuDeChinh("Tìm sản phẩm chống nắng phù hợp với bạn");
        banner.setMoTa("Trả lời vài câu hỏi ngắn — hệ thống SUNOVA sẽ phân tích làn da và gợi ý sản phẩm hoàn hảo dành riêng cho bạn.");
        banner.setNutText("Làm Quiz Ngay");
        banner.setLinkUrl("/quiz");
    }
}
