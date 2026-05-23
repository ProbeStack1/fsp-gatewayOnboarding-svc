package com.probestack.forgesphere.onboarding.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection;

public interface BusinessUnitCollectionRepository extends MongoRepository<BusinessUnitCollection, String> {
}

