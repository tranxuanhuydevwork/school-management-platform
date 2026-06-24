package com.golearn.myf3school_backend.application_service.dtos.request;
 
import lombok.*;
import java.time.Instant;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageDto {
    private Long   id;
    private Long   senderId;
    private String senderName;
    private String senderRole;
    private Long   receiverId;
    private String content;
    private Instant timestamp;
    private boolean isRead;
}