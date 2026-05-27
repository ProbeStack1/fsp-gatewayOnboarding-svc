package com.probestack.forgesphere.onboarding.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.probestack.forgesphere.onboarding.constant.AppConstants;
import com.probestack.forgesphere.onboarding.dto.ApiResponse;
import com.probestack.forgesphere.onboarding.dto.GatewayOrganizationEnvironmentTypeResponse;
import com.probestack.forgesphere.onboarding.dto.UserGatewayOrganizationsResponse;
import com.probestack.forgesphere.onboarding.service.UserGatewayOrganizationsService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class UserGatewayOrganizationsController {

    private final UserGatewayOrganizationsService service;

    public UserGatewayOrganizationsController(UserGatewayOrganizationsService service) {
        this.service = service;
    }

    @GetMapping("/user/{userid}/gateway-organizations")
    public ResponseEntity<ApiResponse<UserGatewayOrganizationsResponse>> getGatewayOrganizations(
            @PathVariable String userid) {
        UserGatewayOrganizationsResponse resp = service.getGatewayOrganizationsForUserEmail(userid);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Gateway organizations fetched successfully", resp));
    }

    @GetMapping("/gateway-organizations/{gatewayOrganizationId}/environment-type")
    public ResponseEntity<ApiResponse<GatewayOrganizationEnvironmentTypeResponse>> getEnvironmentTypeByGatewayOrganizationId(
            @PathVariable String gatewayOrganizationId) {
        GatewayOrganizationEnvironmentTypeResponse resp = service.getEnvironmentTypeByGatewayOrganizationId(gatewayOrganizationId);
        if (resp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure("Gateway organization not found", null));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("environmentType fetched successfully", resp));
    }
}



