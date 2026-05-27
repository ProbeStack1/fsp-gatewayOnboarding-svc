package com.probestack.forgesphere.onboarding.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.probestack.forgesphere.onboarding.dto.ApiResponse;
import com.probestack.forgesphere.onboarding.dto.UserGatewayOrganizationsBusinessUnitsResponse;
import com.probestack.forgesphere.onboarding.service.UserGatewayOrganizationsBusinessUnitsService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class UserGatewayOrganizationsBusinessUnitsController {

    private final UserGatewayOrganizationsBusinessUnitsService service;

    public UserGatewayOrganizationsBusinessUnitsController(
            UserGatewayOrganizationsBusinessUnitsService service) {
        this.service = service;
    }

    @GetMapping("/gateway-organizations/{gatewayOrgId}/business-units")
    public ResponseEntity<ApiResponse<UserGatewayOrganizationsBusinessUnitsResponse>> getBusinessUnitsForGatewayOrg(
            @PathVariable String gatewayOrgId) {
        UserGatewayOrganizationsBusinessUnitsResponse resp = service.getBusinessUnitsForGatewayOrg(gatewayOrgId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Business units fetched successfully", resp));
    }
}

