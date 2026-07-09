package com.ims.backend.controller;

import com.ims.backend.dto.payment.PaymentResponse;
import com.ims.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.pay(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentResponse>> myPayments() {
        return ResponseEntity.ok(paymentService.myPayments());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PaymentResponse>> paymentsOfPolicy(@PathVariable Long policyId) {
        return ResponseEntity.ok(paymentService.paymentsOfPolicy(policyId));
    }
}
