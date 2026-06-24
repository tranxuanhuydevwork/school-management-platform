package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.StudentInvoice;
import com.golearn.myf3school_backend.contract.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentInvoiceRepository extends JpaRepository<StudentInvoice, Long> {
    Page<StudentInvoice> findByStudentId(Long studentId, Pageable pageable);
    List<StudentInvoice> findByStudentIdAndStatus(Long studentId, PaymentStatus status);
    List<StudentInvoice> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);
    @Query("SELECT COALESCE(SUM(i.finalAmount), 0) FROM StudentInvoice i WHERE i.student.id = :studentId AND i.status = 'PENDING'")
    BigDecimal sumPendingByStudent(@Param("studentId") Long studentId);
}
