package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.request.ChatContactDto;
import com.golearn.myf3school_backend.application_service.dtos.request.ChatMessageDto;
import com.golearn.myf3school_backend.infrastructure.entity.ChatMessage;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.infrastructure.repository.ChatMessageRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository repo;
    private final UserRepository userRepo;

    // ── Save & persist ────────────────────────────────────────
    @Transactional
    public ChatMessageDto save(ChatMessageDto dto) {
        ChatMessage entity = ChatMessage.builder()
                .senderId(dto.getSenderId())
                .senderName(dto.getSenderName())
                .senderRole(dto.getSenderRole())
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .sentAt(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())
                .isRead(false)
                .build();

        entity = repo.save(entity);
        return toDto(entity);
    }

    // ── History: dùng List + sort ASC để tránh Page đảo thứ tự ──
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getHistory(Long userId, Long partnerId, int page, int size) {
        // Dùng findConversationList (không phân trang) → sort đảm bảo ASC
        List<ChatMessage> all = repo.findConversationList(userId, partnerId);

        // Áp dụng phân trang thủ công nếu cần
        int from = page * size;
        int to   = Math.min(from + size, all.size());
        if (from >= all.size()) return Collections.emptyList();

        return all.subList(from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Contact list ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ChatContactDto> getContacts(Long userId) {
        List<Long> partnerIds = repo.findPartnerIds(userId);

        return partnerIds.stream()
                .map(pid -> {
                    User u = userRepo.findById(pid).orElse(null);
                    if (u == null) return null;

                    ChatMessage last  = repo.findLastMessage(userId, pid);
                    int         unread = repo.countUnread(pid, userId);

                    String role = u.getRoles().stream()
                            .map(r -> r.getName().name())
                            .findFirst().orElse("STUDENT");

                    return ChatContactDto.builder()
                            .userId(u.getId())
                            .name(u.getFullName())
                            .role(role)
                            .avatarUrl(u.getAvatarUrl())
                            .lastMessage(last != null ? last.getContent() : null)
                            .lastTime(last != null ? last.getSentAt() : null)
                            .unread(unread)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        c -> c.getLastTime() != null ? c.getLastTime() : Instant.EPOCH,
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    // ── Mark read ─────────────────────────────────────────────
    @Transactional
    public void markRead(Long fromId, Long toId) {
        repo.markAsRead(fromId, toId);
    }

    // ── Mapper ────────────────────────────────────────────────
    private ChatMessageDto toDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderRole(m.getSenderRole())
                .receiverId(m.getReceiverId())
                .content(m.getContent())
                .timestamp(m.getSentAt())
                .isRead(m.isRead())
                .build();
    }
}