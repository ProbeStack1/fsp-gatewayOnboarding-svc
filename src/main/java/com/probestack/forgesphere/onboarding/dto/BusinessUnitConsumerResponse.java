package com.probestack.forgesphere.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitConsumerResponse {
    private String id;
    private String consumerId;
    private String name;
}

