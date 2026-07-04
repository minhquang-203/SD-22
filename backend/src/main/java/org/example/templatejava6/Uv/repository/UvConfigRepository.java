package org.example.templatejava6.Uv.repository;

import org.example.templatejava6.Uv.entity.UvConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UvConfigRepository extends JpaRepository<UvConfig,Long> {
}
