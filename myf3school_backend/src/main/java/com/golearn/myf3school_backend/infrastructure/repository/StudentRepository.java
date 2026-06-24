package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Long> { // ← thêm Long

    Optional<StudentProfile> findByUserId(Long userId); // ← đổi Optional<Object> → Optional<StudentProfile>
}