package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.contract.enums.ClubActivityStatus;
import com.golearn.myf3school_backend.infrastructure.entity.ClubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubActivityRepository extends JpaRepository<ClubActivity, Long> {

    /** Sự kiện của một CLB, sắp xếp gần nhất lên đầu */
    List<ClubActivity> findByClubIdOrderByStartTimeDesc(Long clubId);

    /** Sự kiện sắp tới của một CLB */
    List<ClubActivity> findByClubIdAndStatusOrderByStartTimeAsc(
            Long clubId, ClubActivityStatus status);

    /** Tất cả sự kiện toàn trường, sắp xếp theo thời gian */
    @Query("""
        SELECT a FROM ClubActivity a
        JOIN FETCH a.club c
        WHERE c.status = 'ACTIVE'
        ORDER BY a.startTime DESC
        """)
    List<ClubActivity> findAllActiveClubActivities();

    /** Đếm sự kiện sắp tới của CLB */
    long countByClubIdAndStatus(Long clubId, ClubActivityStatus status);
}