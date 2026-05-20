package com.probestack.forgesphere.onboarding.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.probestack.forgesphere.onboarding.model.OnboardingApplication;

public interface OnboardingApplicationRepository extends MongoRepository<OnboardingApplication, String> {
    Optional<OnboardingApplication> findByApprovalToken(String approvalToken);
}

