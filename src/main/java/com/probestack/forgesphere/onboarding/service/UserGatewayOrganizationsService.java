package com.probestack.forgesphere.onboarding.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.dto.GatewayOrganizationEnvironmentTypeResponse;
import com.probestack.forgesphere.onboarding.dto.GatewayOrganizationResponse;

import com.probestack.forgesphere.onboarding.dto.UserGatewayOrganizationsResponse;
import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection;
import com.probestack.forgesphere.onboarding.model.OnboardingApplication;
import com.probestack.forgesphere.onboarding.repository.BusinessUnitCollectionRepository;
import com.probestack.forgesphere.onboarding.repository.OnboardingApplicationRepository;

@Service
public class UserGatewayOrganizationsService {

    private final BusinessUnitCollectionRepository businessUnitRepository;
    private final OnboardingApplicationRepository onboardingRepository;

    public UserGatewayOrganizationsService(BusinessUnitCollectionRepository businessUnitRepository,
            OnboardingApplicationRepository onboardingRepository) {
        this.businessUnitRepository = businessUnitRepository;
        this.onboardingRepository = onboardingRepository;
    }

    public UserGatewayOrganizationsResponse getGatewayOrganizationsForUserEmail(String userEmail) {
        UserGatewayOrganizationsResponse resp = new UserGatewayOrganizationsResponse();
        resp.setMember(false);
        resp.setOnboardingIds(List.of());
        resp.setGatewayOrganizations(List.of());

        if (userEmail == null || userEmail.isBlank()) {
            return resp;
        }

        List<BusinessUnitCollection> matchingBusinessUnits = businessUnitRepository.findByMembersEmail(userEmail);
        if (matchingBusinessUnits == null || matchingBusinessUnits.isEmpty()) {
            return resp;
        }

        resp.setMember(true);

        Set<String> onboardingIds = new LinkedHashSet<>();
        for (BusinessUnitCollection bu : matchingBusinessUnits) {
            if (bu != null && bu.getOnboardingId() != null && !bu.getOnboardingId().isBlank()) {
                onboardingIds.add(bu.getOnboardingId());
            }
        }
        resp.setOnboardingIds(new ArrayList<>(onboardingIds));

        List<GatewayOrganizationResponse> gatewayOrgs = new ArrayList<>();
        Set<String> gatewayOrgIds = new LinkedHashSet<>();

        for (String onboardingId : onboardingIds) {
            if (onboardingId == null || onboardingId.isBlank()) {
                continue;
            }

            OnboardingApplication onboarding = onboardingRepository.findById(onboardingId).orElse(null);
            if (onboarding == null || onboarding.getGatewayOrganizations() == null) {
                continue;
            }

            for (OnboardingApplication.GatewayOrganizationRequest g : onboarding.getGatewayOrganizations()) {
                if (g == null) {
                    continue;
                }

                String gatewayId = g.getId();
                if (gatewayId != null && gatewayOrgIds.contains(gatewayId)) {
                    continue;
                }
                if (gatewayId != null) {
                    gatewayOrgIds.add(gatewayId);
                }

                gatewayOrgs.add(toResponse(g));
            }
        }

        resp.setGatewayOrganizations(gatewayOrgs);
        return resp;
    }

    public GatewayOrganizationEnvironmentTypeResponse getEnvironmentTypeByGatewayOrganizationId(String gatewayOrganizationId) {
        if (gatewayOrganizationId == null || gatewayOrganizationId.isBlank()) {
            return null;
        }

        List<OnboardingApplication> allOnboardingApplications = onboardingRepository.findAll();
        for (OnboardingApplication onboarding : allOnboardingApplications) {
            if (onboarding == null || onboarding.getGatewayOrganizations() == null) {
                continue;
            }

            for (OnboardingApplication.GatewayOrganizationRequest g : onboarding.getGatewayOrganizations()) {
                if (g == null) {
                    continue;
                }
                if (gatewayOrganizationId.equals(g.getId())) {
                    var effectiveCfg = g.getEffectiveConfig();
                    GatewayOrganizationEnvironmentTypeResponse resp = new GatewayOrganizationEnvironmentTypeResponse();
                    if (effectiveCfg != null) {
                        resp.setSelectedEnvironments(effectiveCfg.getSelectedEnvironments());
                    }
                    return resp;
                }

            }
        }

        return null;
    }

    private GatewayOrganizationResponse toResponse(OnboardingApplication.GatewayOrganizationRequest g) {
        GatewayOrganizationResponse out = new GatewayOrganizationResponse();
        if (g == null) {
            return out;
        }
        out.setId(g.getId());
        out.setName(g.getName());
        out.setRegion(g.getRegion());

        // Map config if present
        var effectiveCfg = g.getEffectiveConfig();
        if (effectiveCfg != null) {
            GatewayOrganizationResponse.GatewayConfigResponse cfg = new GatewayOrganizationResponse.GatewayConfigResponse();
            cfg.setEnvironmentType(effectiveCfg.getEnvironmentType());
            cfg.setSelectedEnvironments(effectiveCfg.getSelectedEnvironments());
            cfg.setCustomEnvironments(effectiveCfg.getCustomEnvironments());
            cfg.setExpectedTps(effectiveCfg.getExpectedTps());
            cfg.setExpectedApiRange(effectiveCfg.getExpectedApiRange());
            cfg.setNotes(effectiveCfg.getNotes());
            out.setConfig(cfg);
        }

        return out;
    }
}



