package com.golearn.myf3school_backend.controller;
 
import com.golearn.myf3school_backend.application_service.dtos.request.ChatMessageDto;
import com.golearn.myf3school_backend.application_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messenger;

    // ── WebSocket: receive & route message ────────────────────

    /**
     * Client publishes to: /app/chat.send
     * We persist, then push to recipient's personal queue:
     * /user/{receiverId}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessageDto dto) {
        // Persist
        ChatMessageDto saved = chatService.save(dto);

        // Deliver to recipient
        messenger.convertAndSendToUser(
                String.valueOf(saved.getReceiverId()),
                "/queue/messages",
                saved
        );

        // Also echo back to sender (so sender gets the persisted ID / timestamp)
        messenger.convertAndSendToUser(
                String.valueOf(saved.getSenderId()),
                "/queue/messages",
                saved
        );
    }

    // ── REST: conversation history ────────────────────────────
    @GetMapping("/api/chat/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam Long userId,
            @RequestParam Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        List<ChatMessageDto> msgs = chatService.getHistory(userId, partnerId, page, size);
        // Mark as read while we're at it
        chatService.markRead(partnerId, userId);
        return ResponseEntity.ok(Map.of("data", msgs));
    }

    // ── REST: contact list ────────────────────────────────────
    @GetMapping("/api/chat/contacts/{userId}")
    public ResponseEntity<Map<String, Object>> getContacts(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("data", chatService.getContacts(userId)));
    }

    // ── REST: mark conversation read ──────────────────────────
    @PostMapping("/api/chat/read")
    public ResponseEntity<Void> markRead(
            @RequestParam Long fromId,
            @RequestParam Long toId
    ) {
        chatService.markRead(fromId, toId);
        return ResponseEntity.ok().build();
    }
}