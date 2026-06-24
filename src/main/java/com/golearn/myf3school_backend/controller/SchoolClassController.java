package com.golearn.myf3school_backend.controller;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.infrastructure.entity.SchoolClass;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.repository.SchoolClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/classes") @RequiredArgsConstructor
public class SchoolClassController {
    private final SchoolClassRepository classRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolClass>>> getAll(@RequestParam(required = false) Long academicYearId) {
        List<SchoolClass> classes = academicYearId != null
                ? classRepository.findByAcademicYearId(academicYearId)
                : classRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(classes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClass>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(classRepository.findById(id).orElseThrow(() -> new NotFoundException("Class", id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolClass>> create(@RequestBody SchoolClass schoolClass) {
        return ResponseEntity.ok(ApiResponse.created(classRepository.save(schoolClass)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolClass>> update(@PathVariable Long id, @RequestBody SchoolClass body) {
        SchoolClass cls = classRepository.findById(id).orElseThrow(() -> new NotFoundException("Class", id));
        if (body.getCode() != null) cls.setCode(body.getCode());
        if (body.getName() != null) cls.setName(body.getName());
        if (body.getHomeroomTeacher() != null) cls.setHomeroomTeacher(body.getHomeroomTeacher());
        if (body.getMaxStudents() != null) cls.setMaxStudents(body.getMaxStudents());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", classRepository.save(cls)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (!classRepository.existsById(id)) throw new NotFoundException("Class", id);
        classRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công", null));
    }
}
