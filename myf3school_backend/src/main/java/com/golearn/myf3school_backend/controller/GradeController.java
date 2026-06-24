package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.infrastructure.entity.*;
import com.golearn.myf3school_backend.application_service.dtos.request.GradeRequest;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final StudentGradeRepository   gradeRepository;
    private final GradeComponentRepository componentRepository;
    private final CourseSectionRepository  sectionRepository;
    private final StudentProfileRepository studentRepository;
    private final UserRepository           userRepository;

    // ─────────────────────────────────────────────────────────
    // GET /api/grades/students/{studentId}/sections
    // studentId = student_profiles.id (AuthController.studentId)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/students/{studentId}/sections")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSectionsByStudent(
            @PathVariable Long studentId) {

        log.info("[GRADE] getSections: studentId={}", studentId);

        Long semesterId = sectionRepository.findSemesterId();
        log.info("[GRADE] currentSemesterId={}", semesterId);

        if (semesterId == null) {
            log.warn("[GRADE] Không tìm thấy học kỳ hiện tại!");
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }

        List<CourseSection> sections =
                sectionRepository.findByStudentIdAndSemester(studentId, semesterId);
        log.info("[GRADE] sections found={}", sections.size());

        List<Map<String, Object>> result = sections.stream()
                .map(cs -> Map.<String, Object>of(
                        "sectionId",   cs.getId(),
                        "subjectCode", cs.getSubject().getCode(),
                        "subjectName", cs.getSubject().getName(),
                        "teacherName", cs.getTeacher().getFullName()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/grades/students/{studentId}/sections/{sectionId}
    // studentId = student_profiles.id (AuthController.studentId)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/students/{studentId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentGrades(
            @PathVariable Long studentId,
            @PathVariable Long sectionId) {

        log.info("[GRADE] getGrades: studentId={}, sectionId={}", studentId, sectionId);

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("CourseSection", sectionId));

        List<GradeComponent> components =
                componentRepository.findByCourseSectionIdOrderByOrderIndex(sectionId);
        log.info("[GRADE] components={}", components.size());

        List<StudentGrade> studentGrades =
                gradeRepository.findByStudentIdAndComponentCourseSectionId(studentId, sectionId);
        log.info("[GRADE] grades found={}", studentGrades.size());

        Map<Long, BigDecimal> scoreMap = studentGrades.stream()
                .collect(Collectors.toMap(
                        g -> g.getComponent().getId(),
                        StudentGrade::getScore
                ));

        BigDecimal average = calcWeightedAverage(components, scoreMap);

        List<Map<String, Object>> componentList = components.stream()
                .map(c -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id",       c.getId());
                    map.put("name",     c.getName());
                    map.put("type",     c.getGradeType().name());
                    map.put("weight",   c.getWeight());
                    map.put("maxScore", c.getMaxScore());
                    map.put("score",    scoreMap.get(c.getId())); // null nếu chưa có
                    return map;
                })
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sectionId",   sectionId);
        response.put("subjectCode", section.getSubject().getCode());
        response.put("subjectName", section.getSubject().getName());
        response.put("className",   section.getSchoolClass().getCode());
        response.put("teacherName", section.getTeacher().getFullName());
        response.put("components",  componentList);
        response.put("average",     average);
        response.put("letterGrade", average != null ? toLetterGrade(average) : "-");
        response.put("passed",      average != null
                && average.compareTo(new BigDecimal("5.0")) >= 0);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ── Grade Components CRUD ─────────────────────────────────

    @GetMapping("/sections/{sectionId}/components")
    public ResponseEntity<ApiResponse<List<GradeComponent>>> getComponents(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                componentRepository.findByCourseSectionIdOrderByOrderIndex(sectionId)));
    }

    @PostMapping("/sections/{sectionId}/components")
    public ResponseEntity<ApiResponse<GradeComponent>> createComponent(
            @PathVariable Long sectionId, @RequestBody GradeComponent component) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("CourseSection", sectionId));
        component.setCourseSection(section);
        return ResponseEntity.ok(ApiResponse.created(componentRepository.save(component)));
    }

    @PutMapping("/components/{id}")
    public ResponseEntity<ApiResponse<GradeComponent>> updateComponent(
            @PathVariable Long id, @RequestBody GradeComponent body) {
        GradeComponent comp = componentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("GradeComponent", id));
        if (body.getName()        != null) comp.setName(body.getName());
        if (body.getWeight()      != null) comp.setWeight(body.getWeight());
        if (body.getMaxScore()    != null) comp.setMaxScore(body.getMaxScore());
        if (body.getGradingDate() != null) comp.setGradingDate(body.getGradingDate());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công",
                componentRepository.save(comp)));
    }

    @DeleteMapping("/components/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(@PathVariable Long id) {
        if (!componentRepository.existsById(id))
            throw new NotFoundException("GradeComponent", id);
        componentRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }

    // ── Cập nhật điểm (dùng studentProfileId — internal) ─────

    @PutMapping("/students/{studentProfileId}/components/{componentId}")
    @Transactional
    public ResponseEntity<ApiResponse<StudentGrade>> updateGrade(
            @PathVariable Long studentProfileId,
            @PathVariable Long componentId,
            @RequestBody GradeRequest request) {

        GradeComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new NotFoundException("GradeComponent", componentId));
        StudentProfile student = studentRepository.findById(studentProfileId)
                .orElseThrow(() -> new NotFoundException("Student", studentProfileId));

        if (request.getScore() != null) {
            if (request.getScore().compareTo(BigDecimal.ZERO) < 0)
                throw new BadRequestException("Điểm không thể âm");
            if (request.getScore().compareTo(component.getMaxScore()) > 0)
                throw new BadRequestException("Điểm không thể vượt quá "
                        + component.getMaxScore());
        }

        StudentGrade grade = gradeRepository
                .findByStudentIdAndComponentId(studentProfileId, componentId)
                .orElse(StudentGrade.builder()
                        .student(student).component(component).build());

        grade.setScore(request.getScore());
        grade.setNotes(request.getNotes());
        grade.setGradedAt(LocalDateTime.now());
        if (request.getGradedById() != null)
            userRepository.findById(request.getGradedById())
                    .ifPresent(grade::setGradedBy);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật điểm thành công",
                gradeRepository.save(grade)));
    }

    @PutMapping("/components/{componentId}/bulk")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> bulkUpdateGrades(
            @PathVariable Long componentId,
            @RequestBody GradeRequest request) {

        if (request.getStudentScores() == null || request.getStudentScores().isEmpty())
            throw new BadRequestException("Không có điểm nào để cập nhật");

        GradeComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new NotFoundException("GradeComponent", componentId));

        request.getStudentScores().forEach((studentProfileId, score) -> {
            try {
                studentRepository.findById(studentProfileId).ifPresent(student -> {
                    StudentGrade grade = gradeRepository
                            .findByStudentIdAndComponentId(studentProfileId, componentId)
                            .orElse(StudentGrade.builder()
                                    .student(student).component(component).build());
                    grade.setScore(score);
                    grade.setGradedAt(LocalDateTime.now());
                    gradeRepository.save(grade);
                });
            } catch (Exception e) {
                log.error("Lỗi cập nhật điểm cho học sinh {}: {}",
                        studentProfileId, e.getMessage());
            }
        });

        return ResponseEntity.ok(ApiResponse.ok(
                "Cập nhật " + request.getStudentScores().size()
                        + " điểm thành công", null));
    }

    // ── Helpers ───────────────────────────────────────────────

    private BigDecimal calcWeightedAverage(List<GradeComponent> components,
                                           Map<Long, BigDecimal> scoreMap) {
        BigDecimal totalWeight = components.stream()
                .map(GradeComponent::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return null;
        if (components.stream().noneMatch(c -> scoreMap.get(c.getId()) != null))
            return null;

        BigDecimal weighted = components.stream()
                .filter(c -> scoreMap.get(c.getId()) != null)
                .map(c -> {
                    BigDecimal normalized = scoreMap.get(c.getId())
                            .divide(c.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.TEN);
                    return normalized.multiply(c.getWeight());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return weighted.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private String toLetterGrade(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 9.0) return "A+";
        if (s >= 8.0) return "A";
        if (s >= 7.0) return "B";
        if (s >= 6.0) return "C";
        if (s >= 5.0) return "D";
        return "F";
    }
    @GetMapping("/admin/grades/import")
    public String gradeImportPage() {
        return "admin/course/grade_import";  // Thymeleaf resolve → templates/view/admin/course/grade_import.html
    }
}