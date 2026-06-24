package com.golearn.myf3school_backend.application_service.dtos.request;
 
import lombok.*;
import java.time.Instant;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatContactDto {
    private Long    userId;
    private String  name;
    private String  role;
    private String  avatarUrl;
    private String  lastMessage;
    private Instant lastTime;
    private int     unread;
}
 