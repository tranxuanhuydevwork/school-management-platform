package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.application_service.dtos.response.StudentStatResponse;
import com.golearn.myf3school_backend.infrastructure.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentStatResponse getStats(Long studentId) {

        return StudentStatResponse.builder()
                .gpa(3.45)
                .attendancePercent(92)
                .earnedCredits(100)
                .totalCredits(150)
                .classRank(15)
                .build();
    }
}