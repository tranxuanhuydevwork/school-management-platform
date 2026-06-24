package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    Optional<StudentGrade> findByStudentIdAndComponentId(Long studentId, Long componentId);

    @Query("""
    SELECT sg FROM StudentGrade sg
    JOIN FETCH sg.component c
    WHERE sg.student.id = :studentId     
      AND c.courseSection.id = :sectionId
""")
    List<StudentGrade> findByStudentIdAndComponentCourseSectionId(
            @Param("studentId")  Long studentId,
            @Param("sectionId")  Long sectionId);
}