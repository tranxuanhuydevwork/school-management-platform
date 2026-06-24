package com.golearn.myf3school_backend.controller;

import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.PagedResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.StudentDTO;
import com.golearn.myf3school_backend.application_service.dtos.response.StudentStatResponse;
import com.golearn.myf3school_backend.application_service.service.StudentService;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.repository.StudentProfileRepository;
import com.golearn.myf3school_backend.infrastructure.repository.UserRepository;
import com.golearn.myf3school_backend.infrastructure.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProfileRepository studentRepository;
    private final UserRepository userRepository;
    private final SchoolClassRepository classRepository;
    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        // filter theo class
        if (classId != null) {
            List<StudentProfile> profiles = studentRepository.findByClassId(classId);
            List<StudentDTO> dtos = profiles.stream().map(StudentDTO::from).toList();
            return ResponseEntity.ok(ApiResponse.ok(dtos));
        }

        // phân trang bình thường
        Page<StudentProfile> result = studentRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> getById(@PathVariable Long id) {
        StudentProfile student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student", id));
        return ResponseEntity.ok(ApiResponse.ok(student));
    }

    @GetMapping("/{studentId}/stats")
    public ResponseEntity<ApiResponse<StudentStatResponse>> getStats(
            @PathVariable Long studentId) {

        StudentStatResponse stats = studentService.getStats(studentId);
        return ResponseEntity.ok(ApiResponse.ok("Success", stats));
    }

    @GetMapping("/code/{studentCode}")
    public ResponseEntity<ApiResponse<StudentProfile>> getByCode(@PathVariable String studentCode) {
        StudentProfile student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new NotFoundException("Học sinh không tồn tại với mã: " + studentCode));
        return ResponseEntity.ok(ApiResponse.ok(student));
    }

    @GetMapping("/by-class/{classId}")
    public ResponseEntity<ApiResponse<List<StudentProfile>>> getByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(ApiResponse.ok(studentRepository.findBySchoolClassId(classId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentProfile>> create(@RequestBody StudentProfile student) {
        if (student.getStudentCode() != null && studentRepository.existsByStudentCode(student.getStudentCode())) {
            throw new BadRequestException("Mã học sinh '" + student.getStudentCode() + "' đã tồn tại");
        }
        StudentProfile saved = studentRepository.save(student);
        return ResponseEntity.ok(ApiResponse.created(saved));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> update(
            @PathVariable Long id, @RequestBody StudentProfile body) {

        StudentProfile student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student", id));

        if (body.getSchoolClass() != null)          student.setSchoolClass(body.getSchoolClass());
        if (body.getEnrollmentDate() != null)        student.setEnrollmentDate(body.getEnrollmentDate());
        if (body.getAcademicRank() != null)          student.setAcademicRank(body.getAcademicRank());       // rank → academicRank
        if (body.getGpa() != null)                   student.setGpa(body.getGpa());
        if (body.getEmergencyContactName() != null)  student.setEmergencyContactName(body.getEmergencyContactName());   // field mới
        if (body.getEmergencyContactPhone() != null) student.setEmergencyContactPhone(body.getEmergencyContactPhone()); // field mới

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", studentRepository.save(student)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!studentRepository.existsById(id)) throw new NotFoundException("Student", id);
        studentRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }
    /**
     * GET /api/students?classId=1
     * Frontend attendance.html gọi endpoint này để load danh sách học sinh
     */
    /**
     * GET /api/students?classId=1
     * Frontend attendance.html gọi endpoint này để load danh sách học sinh
     */


}