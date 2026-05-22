package com.probestack.forgesphere.onboarding.service;

import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.model.SecretEntity;
import com.probestack.forgesphere.onboarding.repository.SecretRepository;

@Service
public class SecretsService {

    private final SecretRepository secretRepository;

    public SecretsService(SecretRepository secretRepository) {
        this.secretRepository = secretRepository;
    }

    /**
     * Reads the raw secret string from MongoDB.
     */
    public String getSecretValue(String name) {
        SecretEntity secret = secretRepository
                .findByName(name)
                .orElseThrow(() -> new IllegalStateException("Secret not found: " + name));

        return secret.getValue();
    }
}

