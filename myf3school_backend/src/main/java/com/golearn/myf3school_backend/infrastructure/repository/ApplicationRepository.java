package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.contract.enums.RequestStatus;
import com.golearn.myf3school_backend.contract.enums.RequestType;
import com.golearn.myf3school_backend.infrastructure.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ── Student queries (đã có) ───────────────────────────────
    List<Application> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Application> findByStudentIdAndStatusOrderByCreatedAtDesc(
            Long studentId, RequestStatus status);

    long countByStudentIdAndStatus(Long studentId, RequestStatus status);

    // ── Admin queries (mới thêm) ──────────────────────────────

    /** Tất cả đơn, phân trang — dùng cho trang admin */
    Page<Application> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Filter theo status */
    Page<Application> findByStatusOrderByCreatedAtDesc(
            RequestStatus status, Pageable pageable);

    /** Filter theo requestType */
    Page<Application> findByRequestTypeOrderByCreatedAtDesc(
            RequestType requestType, Pageable pageable);

    /** Filter theo cả status + requestType */
    Page<Application> findByStatusAndRequestTypeOrderByCreatedAtDesc(
            RequestStatus status, RequestType requestType, Pageable pageable);

    /** Đếm đơn PENDING — dùng cho badge sidebar */
    long countByStatus(RequestStatus status);
}