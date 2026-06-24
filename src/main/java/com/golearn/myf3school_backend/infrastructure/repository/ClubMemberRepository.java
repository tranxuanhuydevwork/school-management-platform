package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.contract.enums.ClubMemberRole;
import com.golearn.myf3school_backend.contract.enums.ClubMemberStatus;
import com.golearn.myf3school_backend.infrastructure.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    /** Lấy tất cả thành viên của một CLB */
    @Query("""
        SELECT cm FROM ClubMember cm
        JOIN FETCH cm.student s
        JOIN FETCH s.user u
        WHERE cm.club.id = :clubId
        ORDER BY cm.clubRole, u.fullName
        """)
    List<ClubMember> findByClubId(@Param("clubId") Long clubId);

    /** Lấy thành viên theo trạng thái */
    @Query("""
        SELECT cm FROM ClubMember cm
        JOIN FETCH cm.student s
        JOIN FETCH s.user u
        WHERE cm.club.id = :clubId AND cm.status = :status
        """)
    List<ClubMember> findByClubIdAndStatus(@Param("clubId") Long clubId,
                                           @Param("status") ClubMemberStatus status);

    /** Kiểm tra học sinh đã là thành viên chưa */
    Optional<ClubMember> findByClubIdAndStudentId(Long clubId, Long studentId);

    /** CLB của một học sinh (đang active hoặc pending) */
    @Query("""
        SELECT cm FROM ClubMember cm
        JOIN FETCH cm.club c
        WHERE cm.student.id = :studentId
          AND cm.status IN ('ACTIVE', 'PENDING')
        ORDER BY c.name
        """)
    List<ClubMember> findByStudentId(@Param("studentId") Long studentId);

    /** Đếm thành viên active của CLB */
    long countByClubIdAndStatus(Long clubId, ClubMemberStatus status);

    /** Kiểm tra quyền leader */
    @Query("""
        SELECT cm FROM ClubMember cm
        WHERE cm.club.id  = :clubId
          AND cm.student.id = :studentId
          AND cm.status    = 'ACTIVE'
          AND cm.clubRole IN ('PRESIDENT', 'VICE_PRESIDENT')
        """)
    Optional<ClubMember> findLeaderRole(@Param("clubId")    Long clubId,
                                        @Param("studentId") Long studentId);

    /** Cập nhật role */
    @Modifying
    @Query("UPDATE ClubMember cm SET cm.clubRole = :role WHERE cm.id = :id")
    void updateRole(@Param("id") Long id, @Param("role") ClubMemberRole role);
}