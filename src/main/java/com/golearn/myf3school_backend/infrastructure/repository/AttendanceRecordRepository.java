package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.AttendanceRecord;
import com.golearn.myf3school_backend.contract.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySessionId(Long sessionId);
    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);
    long countByStudentIdAndSessionCourseSectionIdAndStatus(Long studentId, Long sectionId, AttendanceStatus status);
    @Query("SELECT ar FROM AttendanceRecord ar JOIN FETCH ar.session s JOIN FETCH s.courseSection cs JOIN FETCH cs.subject WHERE ar.student.id = :studentId AND cs.semester.id = :semesterId ORDER BY s.sessionDate DESC")
    List<AttendanceRecord> findByStudentAndSemester(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);
}
