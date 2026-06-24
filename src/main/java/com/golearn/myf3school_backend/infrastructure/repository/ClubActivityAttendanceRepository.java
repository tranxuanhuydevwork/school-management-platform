package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.ClubActivityAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubActivityAttendanceRepository
        extends JpaRepository<ClubActivityAttendance, Long> {

    List<ClubActivityAttendance> findByActivityId(Long activityId);

    Optional<ClubActivityAttendance> findByActivityIdAndStudentId(
            Long activityId, Long studentId);

    @Query("""
        SELECT a FROM ClubActivityAttendance a
        JOIN FETCH a.activity act
        JOIN FETCH act.club c
        WHERE a.student.id = :studentId
        ORDER BY act.startTime DESC
        """)
    List<ClubActivityAttendance> findByStudentId(@Param("studentId") Long studentId);
}