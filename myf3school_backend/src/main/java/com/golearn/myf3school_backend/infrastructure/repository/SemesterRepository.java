package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    Optional<Semester> findByIsCurrentTrue();
    java.util.List<Semester> findByAcademicYearId(Long academicYearId);
}
