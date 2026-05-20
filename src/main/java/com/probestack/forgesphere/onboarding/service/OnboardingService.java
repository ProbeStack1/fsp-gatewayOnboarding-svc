// OnboardingService.java

package com.probestack.forgesphere.onboarding.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationRequest;
import com.probestack.forgesphere.onboarding.dto.OnboardingApplicationResponse;
import com.probestack.forgesphere.onboarding.dto.SubmitApprovalRequest;
import com.probestack.forgesphere.onboarding.model.OnboardingApplication;
import com.probestack.forgesphere.onboarding.model.OnboardingApplicationStatus;
import com.probestack.forgesphere.onboarding.repository.OnboardingApplicationRepository;

@Service
public class OnboardingService {

    private final OnboardingApplicationRepository repository;
    private final EmailService emailService;

    @Value("${approval.base-url:http://localhost:8081/onboarding}")
    private String approvalBaseUrl;

    public OnboardingService(
            OnboardingApplicationRepository repository,
            EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public OnboardingApplicationResponse createApplication(
            OnboardingApplicationRequest request) {

        Instant now = Instant.now();

        OnboardingApplication app = OnboardingApplication.builder()
                .createdAt(now)
                .updatedAt(now)
                .status(OnboardingApplicationStatus.DRAFT)
                .targetEmail(request.getTargetEmail())
                .company(toCompany(request))
                .stakeholder(toStakeholder(request))
                .gatewayOrganizations(toGatewayOrganizations(request))
                .approvalToken(null)
                .build();

        OnboardingApplication saved = repository.save(app);
        return toResponse(saved);
    }

    public OnboardingApplicationResponse submitForApproval(
            String id,
            SubmitApprovalRequest ignored) {

        OnboardingApplication app = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        if (app.getStatus() == OnboardingApplicationStatus.APPROVED) {
            return toResponse(app);
        }

        String token = UUID.randomUUID().toString();

        app.setApprovalToken(token);
        app.setStatus(OnboardingApplicationStatus.PENDING_APPROVAL);
        app.setUpdatedAt(Instant.now());

        OnboardingApplication saved = repository.save(app);

        if (app.getStakeholder() == null
                || app.getStakeholder().getEmail() == null
                || app.getStakeholder().getEmail().isBlank()) {
            throw new IllegalArgumentException("stakeholder.email is required");
        }

        emailService.sendApprovalEmail(app, token, approvalBaseUrl);

        return toResponse(saved);
    }

    public OnboardingApplicationResponse getApplication(String id) {
        OnboardingApplication app = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        return toResponse(app);
    }

    public com.probestack.forgesphere.onboarding.dto.ApprovalConfirmResponse confirmByToken(
            String token,
            OnboardingApplicationStatus decision) {

        OnboardingApplication app = repository.findByApprovalToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid approval token"));

        if (decision == OnboardingApplicationStatus.APPROVED) {
            app.setStatus(OnboardingApplicationStatus.APPROVED);
        } else if (decision == OnboardingApplicationStatus.REJECTED) {
            app.setStatus(OnboardingApplicationStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Unsupported decision: " + decision);
        }

        app.setUpdatedAt(Instant.now());
        repository.save(app);

        com.probestack.forgesphere.onboarding.dto.ApprovalConfirmResponse response = new com.probestack.forgesphere.onboarding.dto.ApprovalConfirmResponse();
        response.setId(app.getId());
        response.setStatus(app.getStatus());
        return response;
    }

    private OnboardingApplication.Company toCompany(OnboardingApplicationRequest request) {
        if (request == null || request.getCompany() == null) {
            return null;
        }

        return OnboardingApplication.Company.builder()
                .name(request.getCompany().getName())
                .websiteUrl(request.getCompany().getWebsiteUrl())
                .region(request.getCompany().getRegion())
                .build();
    }

    private OnboardingApplication.Stakeholder toStakeholder(OnboardingApplicationRequest request) {
        if (request == null || request.getStakeholder() == null) {
            return null;
        }

        return OnboardingApplication.Stakeholder.builder()
                .firstName(request.getStakeholder().getFirstName())
                .lastName(request.getStakeholder().getLastName())
                .email(request.getStakeholder().getEmail())
                .phone(request.getStakeholder().getPhone())
                .sme(request.getStakeholder().getSme())
                .smeEmail(request.getStakeholder().getSmeEmail())
                .dlEmail(request.getStakeholder().getDlEmail())
                .build();
    }

    private List<OnboardingApplication.GatewayOrganizationRequest> toGatewayOrganizations(OnboardingApplicationRequest request) {
        if (request == null || request.getGatewayOrganizations() == null) {
            return null;
        }

        return request.getGatewayOrganizations().stream().map(gOrg -> {

            OnboardingApplication.GatewayConfig cfg = null;
            if (gOrg.getConfig() != null) {
                cfg = OnboardingApplication.GatewayConfig.builder()
                        .environmentType(gOrg.getConfig().getEnvironmentType())
                        .selectedEnvironments(gOrg.getConfig().getSelectedEnvironments())
                        .customEnvironments(gOrg.getConfig().getCustomEnvironments())
                        .expectedTps(gOrg.getConfig().getExpectedTps())
                        .expectedApiRange(gOrg.getConfig().getExpectedApiRange())
                        .notes(gOrg.getConfig().getNotes())
                        .build();
            }

            return OnboardingApplication.GatewayOrganizationRequest.builder()
                    .id(gOrg.getId())
                    .name(gOrg.getName())
                    .region(gOrg.getRegion())
                    .config(cfg)
                    .build();
        }).collect(Collectors.toList());
    }

    private OnboardingApplicationResponse toResponse(OnboardingApplication app) {
        return OnboardingApplicationResponse.builder()
                .id(app.getId())
                .status(app.getStatus())
                .approvalToken(app.getApprovalToken())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .targetEmail(app.getTargetEmail())
                .stakeholder(mapStakeholderToRequestType(app))
                .company(mapCompanyToRequestType(app))
                .gatewayOrganizations(mapGatewayOrgsToRequestType(app))
                .build();
    }

    private OnboardingApplicationRequest.StakeholderRequest mapStakeholderToRequestType(OnboardingApplication app) {
        if (app.getStakeholder() == null) {
            return null;
        }

        OnboardingApplicationRequest.StakeholderRequest s = new OnboardingApplicationRequest.StakeholderRequest();
        s.setFirstName(app.getStakeholder().getFirstName());
        s.setLastName(app.getStakeholder().getLastName());
        s.setEmail(app.getStakeholder().getEmail());
        s.setPhone(app.getStakeholder().getPhone());
        s.setSme(app.getStakeholder().getSme());
        s.setSmeEmail(app.getStakeholder().getSmeEmail());
        s.setDlEmail(app.getStakeholder().getDlEmail());
        return s;
    }

    private OnboardingApplicationRequest.CompanyRequest mapCompanyToRequestType(OnboardingApplication app) {
        if (app.getCompany() == null) {
            return null;
        }

        OnboardingApplicationRequest.CompanyRequest c = new OnboardingApplicationRequest.CompanyRequest();
        c.setName(app.getCompany().getName());
        c.setWebsiteUrl(app.getCompany().getWebsiteUrl());
        c.setRegion(app.getCompany().getRegion());
        return c;
    }

    private List<OnboardingApplicationRequest.GatewayOrganizationRequest> mapGatewayOrgsToRequestType(OnboardingApplication app) {
        if (app.getGatewayOrganizations() == null) {
            return null;
        }

        return app.getGatewayOrganizations().stream().map(g -> {
            OnboardingApplicationRequest.GatewayOrganizationRequest out = new OnboardingApplicationRequest.GatewayOrganizationRequest();
            out.setId(g.getId());
            out.setName(g.getName());
            out.setRegion(g.getRegion());

            if (g.getConfig() != null) {
                OnboardingApplicationRequest.GatewayOrganizationConfigRequest cfg = new OnboardingApplicationRequest.GatewayOrganizationConfigRequest();
                cfg.setEnvironmentType(g.getConfig().getEnvironmentType());
                cfg.setSelectedEnvironments(g.getConfig().getSelectedEnvironments());
                cfg.setCustomEnvironments(g.getConfig().getCustomEnvironments());
                cfg.setExpectedTps(g.getConfig().getExpectedTps());
                cfg.setExpectedApiRange(g.getConfig().getExpectedApiRange());
                cfg.setNotes(g.getConfig().getNotes());
                out.setConfig(cfg);
            }

            return out;
        }).collect(Collectors.toList());
    }
}

