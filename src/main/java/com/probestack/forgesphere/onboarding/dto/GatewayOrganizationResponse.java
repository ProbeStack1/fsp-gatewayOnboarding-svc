package com.probestack.forgesphere.onboarding.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayOrganizationResponse {
    private String id;
    private String name;
    private String region;

    // Keep config shape lightweight; extend if frontend needs more
    private GatewayConfigResponse config;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayConfigResponse {
        private String environmentType;
        private List<String> selectedEnvironments;
        private String customEnvironments;
        private String expectedTps;
        private String expectedApiRange;
        private String notes;
    }
}

