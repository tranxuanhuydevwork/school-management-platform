package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    // Đã có — giữ nguyên
    List<AttendanceSession> findByCourseSectionIdOrderBySessionDateDesc(Long sectionId);

    Optional<AttendanceSession> findByCourseSectionIdAndSessionDateAndPeriodStart(
            Long sectionId, LocalDate date, Integer period);

    List<AttendanceSession> findByCourseSectionIdAndSessionDateBetween(
            Long sectionId, LocalDate from, LocalDate to);

    long countByCourseSectionId(Long sectionId);

    // Thêm — AttendanceService đang gọi method này
    List<AttendanceSession> findByCourseSectionIdOrderBySessionDateAsc(Long sectionId);
    @Query("SELECT s FROM AttendanceSession s " +
            "JOIN FETCH s.courseSection cs " +
            "JOIN FETCH cs.subject " +
            "JOIN FETCH cs.schoolClass " +
            "WHERE s.takenBy.id = :teacherId " +
            "ORDER BY s.sessionDate DESC, s.takenAt DESC")
    List<AttendanceSession> findByTakenByIdOrderBySessionDateDesc(
            @Param("teacherId") Long teacherId,
            @Param("limit")     // dùng Pageable thay @Limit cho portable hơn
            org.springframework.data.domain.Pageable pageable);

    // Overload không dùng Pageable (truyền int size từ controller):
    default List<AttendanceSession> findByTakenByIdOrderBySessionDateDesc(Long teacherId, int size) {
        return findByTakenByIdOrderBySessionDateDesc(
                teacherId,
                org.springframework.data.domain.PageRequest.of(0, size));
    }

}