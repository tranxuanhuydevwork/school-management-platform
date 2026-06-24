package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lấy danh sách thông báo của user, mới nhất trước.
     * countQuery tách riêng → tránh Hibernate warning HHH90003004
     * khi dùng JOIN FETCH + Pageable cùng lúc.
     */
    @Query(
            value = """
            SELECT n FROM Notification n
            JOIN FETCH n.user
            WHERE n.user.id = :userId
            ORDER BY n.createdAt DESC
        """,
            countQuery = """
            SELECT COUNT(n) FROM Notification n
            WHERE n.user.id = :userId
        """
    )
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable);

    /**
     * Đếm số thông báo chưa đọc — dùng cho badge số trên Flutter.
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Đánh dấu tất cả thông báo chưa đọc của user là đã đọc.
     * Dùng CURRENT_TIMESTAMP của DB thay vì LocalDateTime.now() Java
     * để tránh lệch timezone.
     */
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = CURRENT_TIMESTAMP
        WHERE n.user.id  = :userId
          AND n.isRead   = false
    """)
    void markAllAsRead(@Param("userId") Long userId);
}