package com.ims.backend.dto.claim;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimReviewRequest {

    @NotNull(message = "Status is required")
    @Pattern(regexp = "UNDER_REVIEW|APPROVED|REJECTED",
            message = "Status must be UNDER_REVIEW, APPROVED or REJECTED")
    private String status;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
