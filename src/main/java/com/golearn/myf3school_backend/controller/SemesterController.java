package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.entity.Semester;
import com.golearn.myf3school_backend.infrastructure.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterRepository semesterRepository;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrent() {
        Semester s = semesterRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new NotFoundException("Không có học kỳ hiện tại"));
        return ResponseEntity.ok(ApiResponse.ok(toDto(s)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll() {
        List<Map<String, Object>> list = semesterRepository
                .findAll(Sort.by(Sort.Direction.DESC, "startDate"))
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    private Map<String, Object> toDto(Semester s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",             s.getId());
        map.put("name",           s.getName());
        map.put("semesterNumber", s.getSemesterNumber());
        map.put("startDate",      s.getStartDate());
        map.put("endDate",        s.getEndDate());
        map.put("isCurrent",      s.getIsCurrent());
        return map;
    }
}