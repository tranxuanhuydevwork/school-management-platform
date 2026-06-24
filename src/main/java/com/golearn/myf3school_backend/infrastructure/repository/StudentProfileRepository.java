package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
    Optional<StudentProfile> findByStudentCode(String code);
    Optional<StudentProfile> findByUserId(Long userId);
    boolean existsByStudentCode(String code);
    List<StudentProfile> findBySchoolClassId(Long classId);
    @Query("SELECT sp FROM StudentProfile sp JOIN sp.user u WHERE sp.schoolClass.id = :classId AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<StudentProfile> findByClassIdWithSearch(@Param("classId") Long classId, @Param("search") String search, Pageable pageable);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user " +
            "WHERE sp.schoolClass.id = :classId ORDER BY sp.studentCode ASC")
    List<StudentProfile> findByClassId(@Param("classId") Long classId);
}
