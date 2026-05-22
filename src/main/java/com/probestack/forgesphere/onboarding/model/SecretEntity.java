package com.probestack.forgesphere.onboarding.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "secrets")
public class SecretEntity {

    @Id
    private String id;

    private String name;

    /**
     * Stored as a single string value, e.g. "mail-api-key=SG....".
     */
    private String value;
}

