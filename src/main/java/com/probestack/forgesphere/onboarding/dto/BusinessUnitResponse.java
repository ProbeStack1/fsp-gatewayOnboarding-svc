package com.probestack.forgesphere.onboarding.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitResponse {

    private String id;

    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;

    private String teamName;
    private String applicationName;
    private String applicationId;

    private String projectOwner;
    private String ownerEmail;
    private String projectSME;
    private String projectSMEEmail;
    private String projectDLEmail;

    private String expectedGoLiveDate;

    private String testerName;
    private String testerEmail;

    private String servicenowGroupName;
    private String servicenowEmail;

    private List<BusinessUnitMemberResponse> members;
    private List<BusinessUnitConsumerResponse> consumers;
}

