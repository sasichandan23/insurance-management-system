package com.ims.backend.dto.payment;

import com.ims.backend.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long policyId;
    private String policyNumber;
    private String planName;
    private String customerName;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private BigDecimal amount;
    private String transactionRef;
    private String status;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .policyId(payment.getPolicy().getId())
                .policyNumber(payment.getPolicy().getPolicyNumber())
                .planName(payment.getPolicy().getPlan().getName())
                .customerName(payment.getPolicy().getCustomer().getName())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .amount(payment.getAmount())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus().name())
                .build();
    }
}
