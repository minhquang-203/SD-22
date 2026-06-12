package org.example.templatejava6.common.repository;

import org.example.templatejava6.common.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {

    Optional<VaiTro> findByMaVaiTro(String maVaiTro);
}
