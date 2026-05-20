# TODO

## Persist updated onboarding payload (company + stakeholder + gatewayOrganizations)
- [ ] Update Mongo model `OnboardingApplication` to store:
  - [ ] `company` object (name, websiteUrl, region)
  - [ ] `stakeholder` fields from payload (firstName, lastName, email, phone, sme, smeEmail, dlEmail, etc.)
  - [ ] `gatewayOrganizations` as a list with matching config fields (environmentType, selectedEnvironments, customEnvironments, expectedTps, expectedApiRange, notes)
- [ ] Update request DTO `OnboardingApplicationRequest` to match incoming JSON exactly.
- [ ] Update mapping in `OnboardingService` so all fields are persisted in Mongo.
- [ ] Update response DTO `OnboardingApplicationResponse` to return stored data.
- [ ] Update `EmailService` to use the new `gatewayOrganizations` structure.
- [ ] Update `src/onboarding.json` example payload.
- [ ] Update tests and ensure `mvn test` passes.

