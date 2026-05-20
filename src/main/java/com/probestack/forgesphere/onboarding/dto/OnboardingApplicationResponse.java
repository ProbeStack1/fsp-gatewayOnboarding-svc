package com.probestack.forgesphere.onboarding.dto;

import java.time.Instant;
import java.util.List;

import com.probestack.forgesphere.onboarding.model.OnboardingApplicationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnboardingApplicationResponse {
    private String id;
    private OnboardingApplicationStatus status;
    private String approvalToken; // included only for debugging/testing; can be removed later
    private Instant createdAt;
    private Instant updatedAt;

    private String targetEmail;

    private OnboardingApplicationRequest.StakeholderRequest stakeholder;
    private OnboardingApplicationRequest.CompanyRequest company;
    private List<OnboardingApplicationRequest.GatewayOrganizationRequest> gatewayOrganizations;
}


