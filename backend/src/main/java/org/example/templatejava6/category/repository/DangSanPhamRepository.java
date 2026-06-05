package org.example.templatejava6.category.repository;

import org.example.templatejava6.category.entity.DangSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DangSanPhamRepository extends JpaRepository<DangSanPham, Integer> {
}
