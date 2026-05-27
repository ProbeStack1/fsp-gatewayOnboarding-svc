package com.probestack.forgesphere.onboarding.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGatewayOrganizationsResponse {

    private boolean member;

    // In current flow: a business unit belongs to a single onboarding (via onboardingId),
    // but a user can be member of multiple business units.
    private List<String> onboardingIds;

    private List<GatewayOrganizationResponse> gatewayOrganizations;
}

