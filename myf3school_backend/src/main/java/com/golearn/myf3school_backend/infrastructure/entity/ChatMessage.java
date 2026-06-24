package com.golearn.myf3school_backend.infrastructure.entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
 
@Entity
@Table(name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_sender",   columnList = "sender_id"),
        @Index(name = "idx_chat_receiver", columnList = "receiver_id"),
        @Index(name = "idx_chat_pair",     columnList = "sender_id, receiver_id, sent_at"),
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "sender_id",   nullable = false) private Long senderId;
    @Column(name = "sender_name", nullable = false) private String senderName;
    @Column(name = "sender_role", nullable = false) private String senderRole;
 
    @Column(name = "receiver_id", nullable = false) private Long receiverId;
 
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
 
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
 
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
 
    @Column(name = "read_at")
    private Instant readAt;
}
 
 