package com.probestack.forgesphere.onboarding.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.probestack.forgesphere.onboarding.model.SecretEntity;

public interface SecretRepository extends MongoRepository<SecretEntity, String> {
    Optional<SecretEntity> findByName(String name);
}

