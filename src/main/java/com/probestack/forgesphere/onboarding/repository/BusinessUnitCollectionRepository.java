package com.probestack.forgesphere.onboarding.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection;


public interface BusinessUnitCollectionRepository extends MongoRepository<BusinessUnitCollection, String> {

    // member email match
    List<BusinessUnitCollection> findByMembersEmail(String email);
}


