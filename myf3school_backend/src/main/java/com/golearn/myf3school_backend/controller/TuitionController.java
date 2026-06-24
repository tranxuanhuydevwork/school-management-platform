package com.golearn.myf3school_backend.controller;
import com.golearn.myf3school_backend.application_service.dtos.response.ApiResponse;
import com.golearn.myf3school_backend.application_service.dtos.response.PagedResponse;
import com.golearn.myf3school_backend.infrastructure.entity.StudentInvoice;
import com.golearn.myf3school_backend.contract.enums.PaymentStatus;
import com.golearn.myf3school_backend.application_service.exception.BadRequestException;
import com.golearn.myf3school_backend.application_service.exception.NotFoundException;
import com.golearn.myf3school_backend.infrastructure.repository.StudentInvoiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/tuition") @RequiredArgsConstructor
public class TuitionController {
    private final StudentInvoiceRepository invoiceRepository;

    @GetMapping("/students/{studentId}/invoices")
    public ResponseEntity<ApiResponse<PagedResponse<StudentInvoice>>> getInvoices(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.of(invoiceRepository.findByStudentId(studentId, PageRequest.of(page, size)))));
    }

    @GetMapping("/students/{studentId}/outstanding")
    public ResponseEntity<ApiResponse<List<StudentInvoice>>> getOutstanding(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceRepository.findByStudentIdAndStatus(studentId, PaymentStatus.PENDING)));
    }

    @GetMapping("/students/{studentId}/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(@PathVariable Long studentId) {
        BigDecimal balance = invoiceRepository.sumPendingByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("studentId", studentId, "balance", balance)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentInvoice>> create(@RequestBody StudentInvoice invoice) {
        return ResponseEntity.ok(ApiResponse.created(invoiceRepository.save(invoice)));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<StudentInvoice>> markPaid(
            @PathVariable Long id,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String transactionRef) {
        StudentInvoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new NotFoundException("Invoice", id));
        if (invoice.getStatus() == PaymentStatus.PAID) throw new BadRequestException("Hóa đơn đã được thanh toán");
        invoice.setStatus(PaymentStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaymentMethod(paymentMethod);
        invoice.setTransactionRef(transactionRef);
        return ResponseEntity.ok(ApiResponse.ok("Thanh toán thành công", invoiceRepository.save(invoice)));
    }
}
