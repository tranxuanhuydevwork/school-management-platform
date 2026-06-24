package com.golearn.myf3school_backend.infrastructure.entity;
import com.golearn.myf3school_backend.contract.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "student_invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentInvoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 30) private String invoiceNumber;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false) private StudentProfile student;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal amount;
    @Column(precision = 15, scale = 2) @Builder.Default private BigDecimal discount = BigDecimal.ZERO;
    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2) private BigDecimal finalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "paid_at") private LocalDateTime paidAt;
    @Column(name = "payment_method", length = 50) private String paymentMethod;
    @Column(name = "transaction_ref", length = 100) private String transactionRef;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "created_at", updatable = false) @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at") @Builder.Default private LocalDateTime updatedAt = LocalDateTime.now();
    @PreUpdate public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
