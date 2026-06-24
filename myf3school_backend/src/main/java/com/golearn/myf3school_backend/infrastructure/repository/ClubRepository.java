package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.contract.enums.ClubStatus;
import com.golearn.myf3school_backend.infrastructure.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByCode(String code);

    List<Club> findByStatus(ClubStatus status);

    /**
     * Tìm kiếm CLB theo tên.
     * Không có category trong entity — filter bằng tên club.
     */
    @Query("""
        SELECT c FROM Club c
        WHERE c.status = 'ACTIVE'
          AND (:search IS NULL
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY c.name
        """)
    List<Club> search(@Param("search") String search);

    /** CLB do một giáo viên cố vấn */
    List<Club> findByAdvisorId(Long advisorId);

    /** CLB mà một học sinh là chủ tịch */
    List<Club> findByPresidentId(Long presidentId);
}