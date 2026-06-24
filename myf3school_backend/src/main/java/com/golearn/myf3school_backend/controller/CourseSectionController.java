package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.CourseSectionDTO;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.entity.CourseSection;
import com.golearn.myf3school_backend.infrastructure.repository.CourseSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController                          // ← đổi từ @Controller → @RestController
@RequestMapping("/api/course-sections")  // ← prefix /api + chuyển lên class
@RequiredArgsConstructor                 // ← inject repo qua constructor (Lombok)
public class CourseSectionController {

    private final CourseSectionRepository sectionRepository;  // ← inject

    /**
     * GET /api/course-sections?teacherId=X&semesterId=Y
     * GET /api/course-sections?classId=X&semesterId=Y
     * GET /api/course-sections   (lấy tất cả — dùng cho admin)
     */


    /**
     * GET /api/course-sections/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseSection>> getById(@PathVariable Long id) {
        CourseSection cs = sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CourseSection", id));
        return ResponseEntity.ok(ApiResponse.ok(cs));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSectionDTO>>> getSections(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long classId) {

        List<CourseSection> result;

        if (teacherId != null) {
            result = sectionRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
        } else if (classId != null) {
            result = sectionRepository.findByClassIdAndSemesterId(classId, semesterId);
        } else {
            result = sectionRepository.findAll();
        }

        // Map sang DTO — tránh serialize proxy/lazy field
        List<CourseSectionDTO> dtos = result.stream()
                .map(CourseSectionDTO::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }
}