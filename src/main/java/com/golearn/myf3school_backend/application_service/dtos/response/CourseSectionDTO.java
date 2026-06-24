package com.golearn.myf3school_backend.application_service.dtos.response;

import com.golearn.myf3school_backend.infrastructure.entity.CourseSection;
import lombok.Data;

@Data
public class CourseSectionDTO {
    private Long   id;
    private SubjectDTO   subject;
    private SchoolClassDTO schoolClass;

    public static CourseSectionDTO from(CourseSection cs) {
        CourseSectionDTO dto = new CourseSectionDTO();
        dto.setId(cs.getId());

        if (cs.getSubject() != null) {
            SubjectDTO s = new SubjectDTO();
            s.setId(cs.getSubject().getId());
            s.setCode(cs.getSubject().getCode());
            s.setName(cs.getSubject().getName());
            dto.setSubject(s);
        }

        if (cs.getSchoolClass() != null) {
            SchoolClassDTO c = new SchoolClassDTO();
            c.setId(cs.getSchoolClass().getId());
            c.setName(cs.getSchoolClass().getName());
            dto.setSchoolClass(c);
        }

        return dto;
    }

    @Data
    public static class SubjectDTO {
        private Long   id;
        private String code;
        private String name;
    }

    @Data
    public static class SchoolClassDTO {
        private Long   id;
        private String name;
    }
}