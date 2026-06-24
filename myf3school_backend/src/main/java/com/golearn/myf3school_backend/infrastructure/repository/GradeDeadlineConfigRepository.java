package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.GradeDeadlineConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeDeadlineConfigRepository
        extends JpaRepository<GradeDeadlineConfig, Long> {

    /** Lấy đợt chốt điểm đang hiệu lực (isActive = true). */
    Optional<GradeDeadlineConfig> findByIsActiveTrue();
}