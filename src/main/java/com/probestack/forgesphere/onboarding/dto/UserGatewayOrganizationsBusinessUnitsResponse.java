package com.probestack.forgesphere.onboarding.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGatewayOrganizationsBusinessUnitsResponse {

    private boolean member;
    private List<String> onboardingIds;
    private List<GatewayOrganizationResponse> gatewayOrganizations;

    private List<BusinessUnitResponse> businessUnits;
}

