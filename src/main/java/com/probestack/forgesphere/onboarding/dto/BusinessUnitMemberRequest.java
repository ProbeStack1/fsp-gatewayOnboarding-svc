package com.probestack.forgesphere.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitMemberRequest {
    private String id; // optional - used for update/delete
    private String name;
    private String email;
}



