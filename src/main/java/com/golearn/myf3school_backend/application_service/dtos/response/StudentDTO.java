package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import lombok.Data;

@Data
public  class StudentDTO {
    private Long   id;           // = student_profiles.id  ← frontend dùng làm studentId
    private String studentCode;
    private String fullName;     // lấy từ user.fullName

    public static StudentDTO from(StudentProfile sp) {
        StudentDTO dto = new StudentDTO();
        dto.setId(sp.getId());
        dto.setStudentCode(sp.getStudentCode());
        dto.setFullName(sp.getUser() != null ? sp.getUser().getFullName() : "—");
        return dto;
    }
}