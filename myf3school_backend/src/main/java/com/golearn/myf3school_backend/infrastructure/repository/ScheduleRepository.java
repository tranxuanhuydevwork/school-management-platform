package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Lấy toàn bộ lịch của lớp — dùng queryDate để kiểm tra hiệu lực
     * Flutter gọi khi load toàn tuần (by-week)
     */
    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.courseSection cs
        JOIN FETCH cs.subject
        JOIN FETCH cs.teacher
        JOIN FETCH cs.schoolClass
        WHERE cs.schoolClass.id = :classId
          AND s.effectiveFrom <= :queryDate
          AND (s.effectiveTo IS NULL OR s.effectiveTo >= :queryDate)
        ORDER BY s.dayOfWeek, s.periodStart
    """)
    List<Schedule> findCurrentByClass(
            @Param("classId")   Long classId,
            @Param("queryDate") LocalDate queryDate);

    /**
     * Lấy lịch theo ngày cụ thể trong tuần
     * :queryDate dùng để kiểm tra effectiveFrom/effectiveTo
     * :day      = ISO weekday (1=Monday … 7=Sunday)
     */
    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.courseSection cs
        JOIN FETCH cs.subject
        JOIN FETCH cs.teacher
        JOIN FETCH cs.schoolClass
        WHERE cs.schoolClass.id = :classId
          AND s.dayOfWeek = :day
          AND s.effectiveFrom <= :queryDate
          AND (s.effectiveTo IS NULL OR s.effectiveTo >= :queryDate)
        ORDER BY s.periodStart
    """)
    List<Schedule> findByClassAndDay(
            @Param("classId")   Long classId,
            @Param("day")       Integer day,
            @Param("queryDate") LocalDate queryDate);

    /**
     * Lấy lịch của giáo viên
     */
    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.courseSection cs
        JOIN FETCH cs.subject
        JOIN FETCH cs.teacher
        JOIN FETCH cs.schoolClass
        WHERE cs.teacher.id = :teacherId
          AND s.effectiveFrom <= :queryDate
          AND (s.effectiveTo IS NULL OR s.effectiveTo >= :queryDate)
        ORDER BY s.dayOfWeek, s.periodStart
    """)
    List<Schedule> findCurrentByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("queryDate") LocalDate queryDate);
}