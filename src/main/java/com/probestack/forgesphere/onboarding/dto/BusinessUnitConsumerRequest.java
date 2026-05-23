package com.probestack.forgesphere.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitConsumerRequest {
    private String id; // optional - used for update/delete
    private String consumerId;
    private String name;
}

