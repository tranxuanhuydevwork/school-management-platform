package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.GradeComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeComponentRepository extends JpaRepository<GradeComponent, Long> {

    /**
     * Lấy tất cả đầu điểm của 1 môn học (course section), sắp xếp theo orderIndex.
     */
    List<GradeComponent> findByCourseSectionIdOrderByOrderIndex(Long courseSectionId);

    /**
     * Lấy đầu điểm kèm thông tin section để tránh N+1.
     */
    @Query("""
        SELECT gc FROM GradeComponent gc
        JOIN FETCH gc.courseSection cs
        JOIN FETCH cs.subject
        WHERE gc.courseSection.id = :sectionId
        ORDER BY gc.orderIndex
    """)
    List<GradeComponent> findWithSectionBySectionId(@Param("sectionId") Long sectionId);
}