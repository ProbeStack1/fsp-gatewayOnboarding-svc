package com.probestack.forgesphere.onboarding.dto;

import java.util.List;
import lombok.Data;

@Data
public class OnboardingApplicationRequest {

    private CompanyRequest company;
    private StakeholderRequest stakeholder;
    private List<GatewayOrganizationRequest> gatewayOrganizations;
    private String targetEmail;

    @Data
    public static class CompanyRequest {
        private String name;
        private String websiteUrl;
        private String region;
    }

    @Data
    public static class StakeholderRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String sme;
        private String smeEmail;
        private String dlEmail;
    }

    @Data
    public static class GatewayOrganizationRequest {
        private String id;
        private String name;
        private String region;
        private GatewayOrganizationConfigRequest config;
    }

    @Data
    public static class GatewayOrganizationConfigRequest {
        private String environmentType;
        private List<String> selectedEnvironments;
        private String customEnvironments;
        private String expectedTps;
        private String expectedApiRange;
        private String notes;
    }
}

