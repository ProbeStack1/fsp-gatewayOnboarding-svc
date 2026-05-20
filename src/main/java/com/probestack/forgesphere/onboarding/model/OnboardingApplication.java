package com.probestack.forgesphere.onboarding.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "onboarding_applications")
public class OnboardingApplication {

    @Id
    private String id;

    private Instant createdAt;
    private Instant updatedAt;

    private OnboardingApplicationStatus status;

    /**
     * Random token used in the approval email link.
     * Stored so we can validate callback.
     */
    @Indexed(unique = true)
    private String approvalToken;

    /**
     * Matches frontend field (info@probestack.com by default).
     */
    private String targetEmail;

    private Company company;

    private Stakeholder stakeholder;

    /**
     * Multiple gateway organizations are allowed.
     */
    private List<GatewayOrganizationRequest> gatewayOrganizations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Company {
        private String name;
        private String websiteUrl;
        private String region;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stakeholder {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String sme;
        private String smeEmail;
        private String dlEmail;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayOrganizationRequest {
        private String id;
        private String name;
        private String region;
        private GatewayConfig config;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayConfig {
        private String environmentType;
        private List<String> selectedEnvironments;
        private String customEnvironments;
        private String expectedTps;
        private String expectedApiRange;
        private String notes;
    }
}

