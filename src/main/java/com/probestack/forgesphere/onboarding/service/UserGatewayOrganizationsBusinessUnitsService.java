package com.probestack.forgesphere.onboarding.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.dto.BusinessUnitResponse;
import com.probestack.forgesphere.onboarding.dto.GatewayOrganizationResponse;
import com.probestack.forgesphere.onboarding.dto.UserGatewayOrganizationsBusinessUnitsResponse;
import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection;
import com.probestack.forgesphere.onboarding.model.OnboardingApplication;
import com.probestack.forgesphere.onboarding.repository.BusinessUnitCollectionRepository;
import com.probestack.forgesphere.onboarding.repository.OnboardingApplicationRepository;

@Service
public class UserGatewayOrganizationsBusinessUnitsService {

    private final BusinessUnitCollectionRepository businessUnitRepository;
    private final OnboardingApplicationRepository onboardingRepository;

    public UserGatewayOrganizationsBusinessUnitsService(
            BusinessUnitCollectionRepository businessUnitRepository,
            OnboardingApplicationRepository onboardingRepository) {
        this.businessUnitRepository = businessUnitRepository;
        this.onboardingRepository = onboardingRepository;
    }

    public UserGatewayOrganizationsBusinessUnitsResponse getBusinessUnitsForGatewayOrg(String gatewayOrgId) {
        UserGatewayOrganizationsBusinessUnitsResponse resp = new UserGatewayOrganizationsBusinessUnitsResponse();
        resp.setMember(false);
        resp.setOnboardingIds(List.of());
        resp.setGatewayOrganizations(List.of());
        resp.setBusinessUnits(List.of());

        if (gatewayOrgId == null || gatewayOrgId.isBlank()) {
            return resp;
        }

        // Determine which onboarding application(s) contain this gateway org id.
        List<String> onboardingIds = new ArrayList<>();
        Set<String> seenOnboarding = new LinkedHashSet<>();
        List<GatewayOrganizationResponse> gatewayOrgs = new ArrayList<>();
        Set<String> seenGatewayOrg = new LinkedHashSet<>();

        for (OnboardingApplication app : onboardingRepository.findAll()) {
            if (app == null || app.getGatewayOrganizations() == null) {
                continue;
            }

            for (OnboardingApplication.GatewayOrganizationRequest g : app.getGatewayOrganizations()) {
                if (g == null) {
                    continue;
                }
                if (gatewayOrgId.equals(g.getId())) {
                    if (app.getId() != null && !app.getId().isBlank() && seenOnboarding.add(app.getId())) {
                        onboardingIds.add(app.getId());
                    }

                    if (g.getId() != null && seenGatewayOrg.add(g.getId())) {
                        gatewayOrgs.add(toGatewayOrganizationResponse(g));
                    }
                }
            }
        }

        if (onboardingIds.isEmpty()) {
            return resp;
        }

        // Business units are linked to onboardingId.
        List<BusinessUnitResponse> businessUnits = new ArrayList<>();
        for (BusinessUnitCollection bu : businessUnitRepository.findAll()) {
            if (bu == null || bu.getOnboardingId() == null) {
                continue;
            }
            if (!onboardingIds.contains(bu.getOnboardingId())) {
                continue;
            }
            businessUnits.add(toBusinessUnitResponse(bu));
        }

        resp.setMember(true);
        resp.setOnboardingIds(onboardingIds);
        resp.setGatewayOrganizations(gatewayOrgs);
        resp.setBusinessUnits(businessUnits);
        return resp;
    }

    private GatewayOrganizationResponse toGatewayOrganizationResponse(OnboardingApplication.GatewayOrganizationRequest g) {
        GatewayOrganizationResponse out = new GatewayOrganizationResponse();
        out.setId(g.getId());
        out.setName(g.getName());
        out.setRegion(g.getRegion());
        return out;
    }

    // Keep only the fields BusinessUnitResponse has in this project.
    private BusinessUnitResponse toBusinessUnitResponse(BusinessUnitCollection bu) {
        if (bu == null) {
            return null;
        }

        BusinessUnitResponse out = new BusinessUnitResponse();
        out.setId(bu.getId());
        out.setCreatedAt(bu.getCreatedAt());
        out.setUpdatedAt(bu.getUpdatedAt());
        out.setTeamName(bu.getTeamName());
        out.setApplicationName(bu.getApplicationName());
        out.setApplicationId(bu.getApplicationId());
        out.setOnboardingId(bu.getOnboardingId());
        out.setProjectOwner(bu.getProjectOwner());
        out.setOwnerEmail(bu.getOwnerEmail());
        out.setProjectSME(bu.getProjectSME());
        out.setProjectSMEEmail(bu.getProjectSMEEmail());
        out.setProjectDLEmail(bu.getProjectDLEmail());
        out.setExpectedGoLiveDate(bu.getExpectedGoLiveDate());
        out.setTesterName(bu.getTesterName());
        out.setTesterEmail(bu.getTesterEmail());
        out.setServicenowGroupName(bu.getServicenowGroupName());
        out.setServicenowEmail(bu.getServicenowEmail());
        out.setMembers(null);
        out.setConsumers(null);
        return out;
    }
}

