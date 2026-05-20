package com.probestack.forgesphere.onboarding.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationRequest;
import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationResponse;
import com.probestack.forgesphere.onboarding.dto.SubmitApprovalRequest;
import com.probestack.forgesphere.onboarding.dto.ApprovalConfirmResponse;
import com.probestack.forgesphere.onboarding.model.OnboardingApplicationStatus;
import com.probestack.forgesphere.onboarding.service.OnboardingService;


@RestController
@RequestMapping("/api/v1")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/applications")
    public ResponseEntity<OnboardingApplicationResponse> create(@RequestBody OnboardingApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.createApplication(request));
    }

    @PostMapping("/applications/{id}/submit-approval")
    public ResponseEntity<OnboardingApplicationResponse> submitApproval(
            @PathVariable String id,
            @RequestBody(required = false) SubmitApprovalRequest body) {
        SubmitApprovalRequest effective = body == null ? new SubmitApprovalRequest() : body;
        return ResponseEntity.ok(onboardingService.submitForApproval(id, effective));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<OnboardingApplicationResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(onboardingService.getApplication(id));
    }

    @GetMapping("/approval/confirm")
    public ResponseEntity<ApprovalConfirmResponse> confirm(
            @RequestParam String token,
            @RequestParam String decision) {

        OnboardingApplicationStatus status;
        try {
            status = OnboardingApplicationStatus.valueOf(decision);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid decision. Use APPROVED or REJECTED");
        }

        ApprovalConfirmResponse response = onboardingService.confirmByToken(token, status);
        return ResponseEntity.ok(response);
    }

}

