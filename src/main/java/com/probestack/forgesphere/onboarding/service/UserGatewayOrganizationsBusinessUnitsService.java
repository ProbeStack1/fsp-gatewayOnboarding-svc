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

    public UserGatewayOrganizationsBusinessUnitsResponse getBusinessUnitsForUser(String userid) {
        UserGatewayOrganizationsBusinessUnitsResponse resp = new UserGatewayOrganizationsBusinessUnitsResponse();
        resp.setMember(false);
        resp.setOnboardingIds(List.of());
        resp.setGatewayOrganizations(List.of());
        resp.setBusinessUnits(List.of());

        if (userid == null || userid.isBlank()) {
            return resp;
        }

        // Admin user: return all business units.
        if (userid.toLowerCase().startsWith("admin@")) {
            List<BusinessUnitResponse> businessUnits = new ArrayList<>();
            Set<String> onboardingIds = new LinkedHashSet<>();

            for (BusinessUnitCollection bu : businessUnitRepository.findAll()) {
                if (bu == null) {
                    continue;
                }
                if (bu.getOnboardingId() != null && !bu.getOnboardingId().isBlank()) {
                    onboardingIds.add(bu.getOnboardingId());
                }
                BusinessUnitResponse mapped = toBusinessUnitResponse(bu);
                if (mapped != null) {
                    businessUnits.add(mapped);
                }
            }

            List<GatewayOrganizationResponse> gatewayOrgs = resolveGatewayOrganizationsFromOnboardingIds(new ArrayList<>(onboardingIds));

            resp.setMember(true);
            resp.setOnboardingIds(new ArrayList<>(onboardingIds));
            resp.setGatewayOrganizations(gatewayOrgs);
            resp.setBusinessUnits(businessUnits);
            return resp;
        }

        // Non-admin: business units where members[].email == userid
        List<BusinessUnitCollection> matchingBusinessUnits = businessUnitRepository.findByMembersEmail(userid);
        if (matchingBusinessUnits == null || matchingBusinessUnits.isEmpty()) {
            return resp;
        }

        Set<String> onboardingIds = new LinkedHashSet<>();
        List<BusinessUnitResponse> businessUnits = new ArrayList<>();

        for (BusinessUnitCollection bu : matchingBusinessUnits) {
            if (bu == null) {
                continue;
            }
            if (bu.getOnboardingId() != null && !bu.getOnboardingId().isBlank()) {
                onboardingIds.add(bu.getOnboardingId());
            }
            BusinessUnitResponse mapped = toBusinessUnitResponse(bu);
            if (mapped != null) {
                businessUnits.add(mapped);
            }
        }

        List<GatewayOrganizationResponse> gatewayOrgs = resolveGatewayOrganizationsFromOnboardingIds(new ArrayList<>(onboardingIds));

        resp.setMember(true);
        resp.setOnboardingIds(new ArrayList<>(onboardingIds));
        resp.setGatewayOrganizations(gatewayOrgs);
        resp.setBusinessUnits(businessUnits);
        return resp;
    }

    private List<GatewayOrganizationResponse> resolveGatewayOrganizationsFromOnboardingIds(List<String> onboardingIds) {
        if (onboardingIds == null || onboardingIds.isEmpty()) {
            return List.of();
        }

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
                if (g == null || g.getId() == null) {
                    continue;
                }
                if (gatewayOrgIds.add(g.getId())) {
                    gatewayOrgs.add(toGatewayOrganizationResponse(g));
                }
            }
        }

        return gatewayOrgs;
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

