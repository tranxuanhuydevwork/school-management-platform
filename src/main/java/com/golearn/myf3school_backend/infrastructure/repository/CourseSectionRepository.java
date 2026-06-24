package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    // ── Dùng cho AttendanceService: lấy sections theo lớp học + học kỳ ──
    //    CourseSection.schoolClass.id  →  class_id trong DB
    //    CourseSection.semester.id     →  semester_id trong DB
    @Query("SELECT cs FROM CourseSection cs " +
            "JOIN FETCH cs.subject " +
            "WHERE cs.schoolClass.id = :classId " +
            "AND cs.semester.id = :semesterId")
    List<CourseSection> findByClassIdAndSemesterId(
            @Param("classId")    Long classId,
            @Param("semesterId") Long semesterId);
    @Query("SELECT cs FROM CourseSection cs " +
            "JOIN FETCH cs.subject " +
            "JOIN FETCH cs.schoolClass " +
            "WHERE cs.teacher.id = :teacherId " +
            "AND (:semesterId IS NULL OR cs.semester.id = :semesterId)")
    List<CourseSection> findByTeacherIdAndSemesterId(
            @Param("teacherId")  Long teacherId,
            @Param("semesterId") Long semesterId);


    // ── Dùng cho web admin: lấy sections theo học kỳ ────────────────────
    List<CourseSection> findBySemesterId(Long semesterId);

    // Lấy học kỳ hiện tại (ví dụ: semester đang active)
    @Query("SELECT s.id FROM Semester s WHERE s.isCurrent = true")
    Long findSemesterId();
    @Query("""
    SELECT cs FROM CourseSection cs
    JOIN FETCH cs.subject
    JOIN FETCH cs.teacher
    JOIN FETCH cs.schoolClass sc
    JOIN StudentProfile sp ON sp.schoolClass.id = sc.id
    WHERE sp.id = :studentId
    AND cs.semester.id = :semesterId
""")
    List<CourseSection> findByStudentIdAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);
}