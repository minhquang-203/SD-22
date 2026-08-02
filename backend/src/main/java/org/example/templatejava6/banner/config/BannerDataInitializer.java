package org.example.templatejava6.banner.config;

import org.example.templatejava6.banner.entity.BannerTrangChu;
import org.example.templatejava6.banner.repository.BannerTrangChuRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đảm bảo có banner quiz mặc định với tiếng Việt đúng encoding
 * (tránh lỗi charset khi seed bằng SQL trên một số máy Windows).
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
        if (bannerTrangChuRepository.count() == 0) {
            BannerTrangChu banner = new BannerTrangChu();
            applyDefaultQuizContent(banner);
            banner.setThuTu(1);
            banner.setTrangThai(true);
            bannerTrangChuRepository.save(banner);
            return;
        }

        bannerTrangChuRepository.findAll().stream()
                .filter(b -> "/quiz".equalsIgnoreCase(String.valueOf(b.getLinkUrl()).trim()))
                .filter(this::looksBrokenEncoding)
                .forEach(b -> {
                    applyDefaultQuizContent(b);
                    bannerTrangChuRepository.save(b);
                });
    }

    private boolean looksBrokenEncoding(BannerTrangChu b) {
        String title = b.getTieuDeChinh() == null ? "" : b.getTieuDeChinh();
        String eyebrow = b.getTieuDe() == null ? "" : b.getTieuDe();
        return title.contains("?") || title.contains("�") || eyebrow.contains("?") || eyebrow.contains("�")
                || (!title.contains("chống") && !title.contains("phù hợp"));
    }

    private void applyDefaultQuizContent(BannerTrangChu banner) {
        banner.setTieuDe("Trắc nghiệm da");
        banner.setTieuDeChinh("Tìm sản phẩm chống nắng phù hợp với bạn");
        banner.setMoTa("Trả lời vài câu hỏi ngắn — hệ thống SUNOVA sẽ phân tích làn da và gợi ý sản phẩm hoàn hảo dành riêng cho bạn.");
        banner.setNutText("Làm Quiz Ngay");
        banner.setLinkUrl("/quiz");
    }
}
