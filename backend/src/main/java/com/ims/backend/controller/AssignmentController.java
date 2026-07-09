package com.ims.backend.controller;

import com.ims.backend.dto.assignment.AssignmentRequest;
import com.ims.backend.dto.assignment.AssignmentResponse;
import com.ims.backend.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> assign(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.assign(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssignmentResponse>> listAll() {
        return ResponseEntity.ok(assignmentService.listAll());
    }

    @GetMapping("/my-customers")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<AssignmentResponse>> myCustomers() {
        return ResponseEntity.ok(assignmentService.myCustomers());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassign(@PathVariable Long id) {
        assignmentService.unassign(id);
        return ResponseEntity.noContent().build();
    }
}
