package com.golearn.myf3school_backend.application_service.service;

import com.golearn.myf3school_backend.infrastructure.entity.GradeComponent;
import com.golearn.myf3school_backend.infrastructure.entity.StudentGrade;
import com.golearn.myf3school_backend.infrastructure.entity.StudentProfile;
import com.golearn.myf3school_backend.infrastructure.repository.GradeComponentRepository;
import com.golearn.myf3school_backend.infrastructure.repository.StudentGradeRepository;
import com.golearn.myf3school_backend.infrastructure.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GradeService — triển khai GradeApplyPort.
 *
 * FIX:
 *  - applyScore() giờ tạo bản ghi mới (INSERT) nếu chưa tồn tại,
 *    thay vì ném RuntimeException như trước.
 *  - Thêm @Transactional(propagation = REQUIRES_NEW) để lỗi ở đây
 *    không rollback transaction của GradeCorrectionService.
 *  - Inject thêm StudentProfileRepository và GradeComponentRepository
 *    để tạo entity khi cần.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService implements GradeApplyPort {

    private final StudentGradeRepository    studentGradeRepository;
    private final StudentProfileRepository  studentProfileRepository;   // FIX: thêm mới
    private final GradeComponentRepository  gradeComponentRepository;   // FIX: thêm mới

    /**
     * Cập nhật điểm thực tế khi khảo thí APPROVED đơn sửa điểm.
     *
     * Luồng xử lý:
     *  1. Tìm StudentGrade theo (studentProfileId, gradeComponentId).
     *  2. Nếu ĐÃ CÓ   → chỉ cần setScore + save.
     *  3. Nếu CHƯA CÓ → tạo bản ghi mới rồi save  ← FIX CHÍNH.
     *     (Trước đây nhánh này ném RuntimeException → import 0/25 thành công)
     *
     * @param studentProfileId  student_profiles.id
     * @param gradeComponentId  grade_components.id (đầu điểm cần cập nhật)
     * @param newScore          điểm mới được duyệt
     * @param approvedBy        users.id của nhân viên khảo thí (để audit sau)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // FIX: tách transaction độc lập
    public void applyScore(Long studentProfileId,
                           Long gradeComponentId,
                           BigDecimal newScore,
                           Long approvedBy) {

        log.info("[GradeService] applyScore: studentProfileId={}, componentId={}, newScore={}",
                studentProfileId, gradeComponentId, newScore);

        // FIX: dùng orElseGet tạo mới thay vì throw exception
        StudentGrade grade = studentGradeRepository
                .findByStudentIdAndComponentId(studentProfileId, gradeComponentId)
                .orElseGet(() -> {
                    log.info("[GradeService] Không tìm thấy bản ghi điểm, tạo mới — " +
                                    "studentProfileId={}, componentId={}",
                            studentProfileId, gradeComponentId);

                    StudentProfile student = studentProfileRepository
                            .findById(studentProfileId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Không tìm thấy StudentProfile #" + studentProfileId));

                    GradeComponent component = gradeComponentRepository
                            .findById(gradeComponentId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Không tìm thấy GradeComponent #" + gradeComponentId));

                    return StudentGrade.builder()
                            .student(student)
                            .component(component)
                            .build();
                });

        grade.setScore(newScore);
        grade.setGradedAt(LocalDateTime.now());
        // TODO: ghi audit log tại đây nếu cần (approvedBy đã có sẵn)

        studentGradeRepository.save(grade);

        log.info("[GradeService] applyScore OK: studentProfileId={}, componentId={}, score={}",
                studentProfileId, gradeComponentId, newScore);
    }
}