package com.probestack.forgesphere.onboarding.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.service.SecretsService;


import com.probestack.forgesphere.onboarding.model.OnboardingApplication;

import com.probestack.forgesphere.onboarding.model.OnboardingApplication.GatewayOrganizationRequest;


@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.to.info}")
    private String infoTo;

    @Value("${sendgrid.template-id}")
    private String templateId;

    private final SecretsService secretsService;

    public EmailService(SecretsService secretsService) {
        this.secretsService = secretsService;
    }

    public void sendApprovalEmail(
            OnboardingApplication app,
            String approvalToken,
            String baseUrl) {

        log.info("Starting SendGrid email process");

        String sendGridApiKey = getSendGridApiKey();

        String requesterName =
                app.getStakeholder() != null
                        ? (app.getStakeholder().getFirstName()
                                        + " "
                                        + app.getStakeholder().getLastName())
                                .trim()
                        : "Unknown User";

        String requesterEmail =
                app.getStakeholder() != null && app.getStakeholder().getEmail() != null
                        ? app.getStakeholder().getEmail()
                        : "unknown@user.com";

        String organization = "N/A";
        String environment = "N/A";
        String region = "N/A";

        var gatewayOrgs = app.getGatewayOrganizations();


        if (gatewayOrgs != null && !gatewayOrgs.isEmpty()) {

            var org = gatewayOrgs.get(0);


            if (org.getName() != null) {
                organization = org.getName();
            }

            if (org.getRegion() != null) {
                region = org.getRegion();
            }

            if (org.getConfig() != null
                    && org.getConfig().getSelectedEnvironments() != null) {

                environment = String.join(
                        ", ",
                        org.getConfig().getSelectedEnvironments());
            }
        }

        String approveUrl =
                baseUrl
                        + "/api/v1/approval/confirm?token="
                        + approvalToken
                        + "&decision=APPROVED";

        String rejectUrl =
                baseUrl
                        + "/api/v1/approval/confirm?token="
                        + approvalToken
                        + "&decision=REJECTED";


        log.info("Mail From      : {}", mailFrom);
        log.info("Mail To        : {}", infoTo);
        log.info("Reply To       : {}", requesterEmail);
        log.info("Template ID    : {}", templateId);
        log.info("Organization   : {}", organization);
        log.info("Environment    : {}", environment);
        log.info("Region         : {}", region);

        String json =
                "{" 
                        + "\"from\":{" 
                        + "\"email\":\"" + escapeJson(mailFrom) + "\"" 
                        + "},"
                        + "\"personalizations\":[{" 
                        + "\"to\":[{" 
                        + "\"email\":\"" + escapeJson(infoTo) + "\"" 
                        + "}],"
                        + "\"dynamic_template_data\":{" 
                        + "\"requesterName\":\"" + escapeJson(requesterName) + "\"," 
                        + "\"organization\":\"" + escapeJson(organization) + "\"," 
                        + "\"environment\":\"" + escapeJson(environment) + "\"," 
                        + "\"region\":\"" + escapeJson(region) + "\"," 
                        + "\"approveUrl\":\"" + escapeJson(approveUrl) + "\"," 
                        + "\"rejectUrl\":\"" + escapeJson(rejectUrl) + "\""
                        + "}"
                        + "}],"
                        + "\"reply_to\":{" 
                        + "\"email\":\"" + escapeJson(requesterEmail) + "\""
                        + "},"
                        + "\"template_id\":\"" + escapeJson(templateId) + "\""
                        + "}";

        log.info("SendGrid Payload:");
        log.info(json);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                        .header("Authorization", "Bearer " + sendGridApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

        try {

            log.info("Sending email to SendGrid...");

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            log.info("SendGrid Response Status : {}", statusCode);
            log.info("SendGrid Response Body   : {}", response.body());

            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException(
                        "SendGrid send failed. status="
                                + statusCode
                                + ", body="
                                + response.body());
            }

            log.info("Email sent successfully via SendGrid");

        } catch (IOException | InterruptedException e) {

            log.error("SendGrid send failed", e);

            Thread.currentThread().interrupt();

            throw new RuntimeException("SendGrid send failed", e);
        }
    }

    private String getSendGridApiKey() {
        // Mongo secret format example:
        // name: SENDGRID_API_KEY
        // value: mail-api-key=SG....
        String rawValue = secretsService.getSecretValue("SENDGRID_API_KEY");
        String prefix = "mail-api-key=";

        if (rawValue == null || !rawValue.startsWith(prefix)) {
            throw new IllegalStateException(
                    "Invalid SENDGRID_API_KEY secret format. Expected prefix: " + prefix);
        }

        return rawValue.substring(prefix.length());
    }

    private static String escapeJson(String s) {

        if (s == null) {
            return "";
        }

        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}

