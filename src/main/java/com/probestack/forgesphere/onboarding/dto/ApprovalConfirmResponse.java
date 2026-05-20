package com.probestack.forgesphere.onboarding.dto;

import com.probestack.forgesphere.onboarding.model.OnboardingApplicationStatus;
import lombok.Data;

@Data
public class ApprovalConfirmResponse {
    private String id;
    private OnboardingApplicationStatus status;
}


