package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Lấy lịch sử hội thoại - dùng List thay vì Page để tránh
     * Spring Data đảo thứ tự khi kết hợp Pageable + ORDER BY.
     * Flutter sẽ sort lại phía client nên đây chỉ cần đúng data.
     */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderId = :a AND m.receiverId = :b)
           OR (m.senderId = :b AND m.receiverId = :a)
        ORDER BY m.sentAt ASC
        """)
    List<ChatMessage> findConversationList(
            @Param("a") Long userId,
            @Param("b") Long partnerId
    );

    /**
     * Phiên bản có phân trang (giữ lại nếu cần sau này).
     * Lưu ý: truyền PageRequest.of(page, size, Sort.by("sentAt").ascending())
     * từ service để đảm bảo thứ tự.
     */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderId = :a AND m.receiverId = :b)
           OR (m.senderId = :b AND m.receiverId = :a)
        ORDER BY m.sentAt ASC
        """)
    Page<ChatMessage> findConversation(
            @Param("a") Long userId,
            @Param("b") Long partnerId,
            Pageable pageable
    );

    /** Count unread messages sent BY fromId TO toId */
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.senderId = :from AND m.receiverId = :to AND m.isRead = false
        """)
    int countUnread(@Param("from") Long from, @Param("to") Long to);

    /** Last message in a conversation */
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.senderId = :a AND m.receiverId = :b)
           OR (m.senderId = :b AND m.receiverId = :a)
        ORDER BY m.sentAt DESC LIMIT 1
        """)
    ChatMessage findLastMessage(@Param("a") Long a, @Param("b") Long b);

    /** IDs of all partners that userId has chatted with */
    @Query("""
        SELECT DISTINCT
            CASE WHEN m.senderId = :uid THEN m.receiverId ELSE m.senderId END
        FROM ChatMessage m
        WHERE m.senderId = :uid OR m.receiverId = :uid
        """)
    List<Long> findPartnerIds(@Param("uid") Long userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP
        WHERE m.senderId = :from AND m.receiverId = :to AND m.isRead = false
        """)
    void markAsRead(@Param("from") Long from, @Param("to") Long to);
}