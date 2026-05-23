package com.probestack.forgesphere.onboarding.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationRequest;
import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationResponse;
import com.probestack.forgesphere.onboarding.dto.SubmitApprovalRequest;
import com.probestack.forgesphere.onboarding.dto.ApprovalConfirmResponse;
import com.probestack.forgesphere.onboarding.model.OnboardingApplicationStatus;
import com.probestack.forgesphere.onboarding.service.OnboardingService;

import java.net.URI;


@RestController
@RequestMapping("/api/v1")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Value("${approval.redirect-url:https://forgesphere.probestack.io/}")
    private String approvalRedirectUrl;

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

    @GetMapping("/applications")
    public ResponseEntity<java.util.List<OnboardingApplicationResponse>> getAll() {
        return ResponseEntity.ok(onboardingService.getAllApplications());
    }


    @GetMapping("/approval/confirm")
    public ResponseEntity<?> confirm(
            @RequestParam String token,
            @RequestParam String decision) {

        OnboardingApplicationStatus status;
        try {
            status = OnboardingApplicationStatus.valueOf(decision);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid decision. Use APPROVED or REJECTED");
        }

        // Update DB first
        try {
            onboardingService.confirmByToken(token, status);

            // Always redirect on success (no JSON body)
            HttpHeaders headers = new HttpHeaders();
            String target = approvalRedirectUrl + "?status=" + (status == null ? "APPROVED" : status.name());
            headers.setLocation(URI.create(target));
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        } catch (Exception ex) {
            // If confirm fails, redirect instead of returning JSON error to the browser/email client
            HttpHeaders headers = new HttpHeaders();
            String target = approvalRedirectUrl + "?status=ERROR";
            headers.setLocation(URI.create(target));
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }


    }


}


