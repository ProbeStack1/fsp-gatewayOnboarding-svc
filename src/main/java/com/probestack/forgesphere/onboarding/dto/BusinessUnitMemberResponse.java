package com.probestack.forgesphere.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitMemberResponse {
    private String id;
    private String name;
    private String email;
    private String role;
}

