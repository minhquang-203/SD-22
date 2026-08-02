package org.example.templatejava6.banner.repository;

import org.example.templatejava6.banner.entity.BannerTrangChu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerTrangChuRepository extends JpaRepository<BannerTrangChu, Integer> {
    List<BannerTrangChu> findByTrangThaiTrueOrderByThuTuAscIdAsc();

    List<BannerTrangChu> findAllByOrderByThuTuAscIdAsc();
}
