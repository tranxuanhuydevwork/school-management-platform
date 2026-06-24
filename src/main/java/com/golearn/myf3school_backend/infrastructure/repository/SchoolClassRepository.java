package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findByAcademicYearId(Long academicYearId);
    List<SchoolClass> findByGradeId(Long gradeId);
    List<SchoolClass> findByHomeroomTeacherId(Long teacherId);
}
