package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.PagedResponse;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.contract.enums.RoleType;
import com.golearn.myf3school_backend.contract.enums.UserStatus;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<User>>> getAll(
            @RequestParam(required = false) RoleType role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<User> users = userRepository.search(role, search,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(users)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username '" + user.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email '" + user.getEmail() + "' đã được sử dụng");
        }
        // TODO: Encode password khi thêm BCrypt
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.created(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> update(
            @PathVariable Long id, @RequestBody User body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        if (body.getFullName() != null) user.setFullName(body.getFullName());
        if (body.getPhone() != null) user.setPhone(body.getPhone());
        if (body.getAddress() != null) user.setAddress(body.getAddress());
        if (body.getGender() != null) user.setGender(body.getGender());
        if (body.getDateOfBirth() != null) user.setDateOfBirth(body.getDateOfBirth());
        if (body.getAvatarUrl() != null) user.setAvatarUrl(body.getAvatarUrl());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", saved));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id, @RequestParam UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        user.setStatus(status);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }
}
