package com.probestack.forgesphere.onboarding.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.probestack.forgesphere.onboarding.dto.BusinessUnitConsumerRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitConsumerResponse;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitMemberRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitMemberResponse;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitResponse;
import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection;
import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection.BusinessUnitConsumer;
import com.probestack.forgesphere.onboarding.model.BusinessUnitCollection.BusinessUnitMember;
import com.probestack.forgesphere.onboarding.repository.BusinessUnitCollectionRepository;

@Service
public class BusinessUnitCollectionService {

    private final BusinessUnitCollectionRepository repository;

    public BusinessUnitCollectionService(BusinessUnitCollectionRepository repository) {
        this.repository = repository;
    }

    public BusinessUnitResponse createOrSubmit(BusinessUnitRequest request) {
        // For now, treat submit as upsert by id if request.id is present; UI payload does not have id,
        // so we always create a new document.
        BusinessUnitCollection entity = toEntity(request, null);
        entity.setId(null);
        entity.setCreatedAt(java.time.Instant.now());
        entity.setUpdatedAt(java.time.Instant.now());
        BusinessUnitCollection saved = repository.save(entity);
        return toResponse(saved);
    }


    public BusinessUnitResponse getById(String id) {
        BusinessUnitCollection entity = repository.findById(id).orElseThrow();
        return toResponse(entity);
    }

    public List<BusinessUnitResponse> getAll() {
        List<BusinessUnitResponse> out = new ArrayList<>();
        for (BusinessUnitCollection entity : repository.findAll()) {
            out.add(toResponse(entity));
        }
        return out;
    }

    public BusinessUnitResponse update(String id, BusinessUnitRequest request) {
        BusinessUnitCollection existing = repository.findById(id).orElseThrow();
        BusinessUnitCollection updated = toEntity(request, existing.getId());
        updated.setId(existing.getId());
        // if UI omits members/consumers, keep them as empty lists
        if (updated.getMembers() == null) {
            updated.setMembers(new ArrayList<>());
        }
        if (updated.getConsumers() == null) {
            updated.setConsumers(new ArrayList<>());
        }
        BusinessUnitCollection saved = repository.save(updated);
        return toResponse(saved);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<BusinessUnitMemberResponse> getMembers(String id) {
        BusinessUnitCollection entity = requireById(id);
        List<BusinessUnitMember> members = Optional.ofNullable(entity.getMembers()).orElse(List.of());
        List<BusinessUnitMemberResponse> out = new ArrayList<>();
        for (BusinessUnitMember m : members) {
            out.add(toMemberResponse(m));
        }
        return out;
    }

    public List<BusinessUnitConsumerResponse> getConsumers(String id) {
        BusinessUnitCollection entity = requireById(id);
        List<BusinessUnitConsumer> consumers = Optional.ofNullable(entity.getConsumers()).orElse(List.of());
        List<BusinessUnitConsumerResponse> out = new ArrayList<>();
        for (BusinessUnitConsumer c : consumers) {
            out.add(toConsumerResponse(c));
        }
        return out;
    }

    public void deleteMember(String businessUnitId, String memberId) {
        BusinessUnitCollection entity = requireById(businessUnitId);
        List<BusinessUnitMember> members = new ArrayList<>(Optional.ofNullable(entity.getMembers()).orElse(List.of()));
        members.removeIf(m -> Objects.equals(m.getId(), memberId));
        entity.setMembers(members);
        repository.save(entity);
    }

    public void deleteConsumer(String businessUnitId, String consumerRecordId) {
        BusinessUnitCollection entity = requireById(businessUnitId);
        List<BusinessUnitConsumer> consumers = new ArrayList<>(Optional.ofNullable(entity.getConsumers()).orElse(List.of()));
        consumers.removeIf(c -> Objects.equals(c.getId(), consumerRecordId));
        entity.setConsumers(consumers);
        repository.save(entity);
    }

    /**
     * PUT /members with an updated list.
     * If member.id is missing, server will assign a stable generated id.
     */
    public List<BusinessUnitMemberResponse> upsertMembers(String businessUnitId, List<BusinessUnitMemberRequest> requests) {
        BusinessUnitCollection entity = requireById(businessUnitId);
        List<BusinessUnitMember> members = new ArrayList<>(Optional.ofNullable(entity.getMembers()).orElse(List.of()));

        for (BusinessUnitMemberRequest r : Optional.ofNullable(requests).orElse(List.of())) {
            BusinessUnitMember incoming = toMemberEntity(r);
            if (incoming.getId() == null || incoming.getId().isBlank()) {
                incoming.setId(java.util.UUID.randomUUID().toString());
            }

            // upsert by member id
            int idx = -1;
            for (int i = 0; i < members.size(); i++) {
                if (Objects.equals(members.get(i).getId(), incoming.getId())) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                members.set(idx, incoming);
            } else {
                members.add(incoming);
            }
        }

        entity.setMembers(members);
        repository.save(entity);

        List<BusinessUnitMemberResponse> out = new ArrayList<>();
        for (BusinessUnitMember m : members) {
            out.add(toMemberResponse(m));
        }
        return out;
    }

    public List<BusinessUnitConsumerResponse> upsertConsumers(String businessUnitId, List<BusinessUnitConsumerRequest> requests) {
        BusinessUnitCollection entity = requireById(businessUnitId);
        List<BusinessUnitConsumer> consumers = new ArrayList<>(Optional.ofNullable(entity.getConsumers()).orElse(List.of()));

        for (BusinessUnitConsumerRequest r : Optional.ofNullable(requests).orElse(List.of())) {
            BusinessUnitConsumer incoming = toConsumerEntity(r);
            if (incoming.getId() == null || incoming.getId().isBlank()) {
                incoming.setId(java.util.UUID.randomUUID().toString());
            }

            int idx = -1;
            for (int i = 0; i < consumers.size(); i++) {
                if (Objects.equals(consumers.get(i).getId(), incoming.getId())) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                consumers.set(idx, incoming);
            } else {
                consumers.add(incoming);
            }
        }

        entity.setConsumers(consumers);
        repository.save(entity);

        List<BusinessUnitConsumerResponse> out = new ArrayList<>();
        for (BusinessUnitConsumer c : consumers) {
            out.add(toConsumerResponse(c));
        }
        return out;
    }

    private BusinessUnitCollection requireById(String id) {
        return repository.findById(id).orElseThrow();
    }

    private BusinessUnitResponse toResponse(BusinessUnitCollection entity) {
        if (entity == null) {
            return null;
        }
        com.probestack.forgesphere.onboarding.dto.BusinessUnitResponse resp = new com.probestack.forgesphere.onboarding.dto.BusinessUnitResponse();
        resp.setId(entity.getId());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        resp.setTeamName(entity.getTeamName());
        resp.setApplicationName(entity.getApplicationName());
        resp.setApplicationId(entity.getApplicationId());
        resp.setProjectOwner(entity.getProjectOwner());
        resp.setOwnerEmail(entity.getOwnerEmail());
        resp.setProjectSME(entity.getProjectSME());
        resp.setProjectSMEEmail(entity.getProjectSMEEmail());
        resp.setProjectDLEmail(entity.getProjectDLEmail());
        resp.setExpectedGoLiveDate(entity.getExpectedGoLiveDate());
        resp.setTesterName(entity.getTesterName());
        resp.setTesterEmail(entity.getTesterEmail());
        resp.setServicenowGroupName(entity.getServicenowGroupName());
        resp.setServicenowEmail(entity.getServicenowEmail());
        resp.setMembers(mapMembers(entity.getMembers()));
        resp.setConsumers(mapConsumers(entity.getConsumers()));
        return resp;
    }

    private List<BusinessUnitMemberResponse> mapMembers(List<BusinessUnitMember> members) {
        List<BusinessUnitMemberResponse> out = new ArrayList<>();
        for (BusinessUnitMember m : Optional.ofNullable(members).orElse(List.of())) {
            out.add(toMemberResponse(m));
        }
        return out;
    }

    private List<BusinessUnitConsumerResponse> mapConsumers(List<BusinessUnitConsumer> consumers) {
        List<BusinessUnitConsumerResponse> out = new ArrayList<>();
        for (BusinessUnitConsumer c : Optional.ofNullable(consumers).orElse(List.of())) {
            out.add(toConsumerResponse(c));
        }
        return out;
    }

    private BusinessUnitCollection toEntity(BusinessUnitRequest request, String existingId) {
        BusinessUnitCollection entity = new BusinessUnitCollection();
        entity.setId(existingId);
        if (request == null) {
            return entity;
        }

        entity.setTeamName(request.getTeamName());
        entity.setApplicationName(request.getApplicationName());
        entity.setApplicationId(request.getApplicationId());

        entity.setProjectOwner(request.getProjectOwner());
        entity.setOwnerEmail(request.getOwnerEmail());
        entity.setProjectSME(request.getProjectSME());
        entity.setProjectSMEEmail(request.getProjectSMEEmail());
        entity.setProjectDLEmail(request.getProjectDLEmail());

        entity.setExpectedGoLiveDate(request.getExpectedGoLiveDate());

        entity.setTesterName(request.getTesterName());
        entity.setTesterEmail(request.getTesterEmail());

        entity.setServicenowGroupName(request.getServicenowGroupName());
        entity.setServicenowEmail(request.getServicenowEmail());

        List<BusinessUnitMember> ms = new ArrayList<>();
        for (BusinessUnitMemberRequest mr : Optional.ofNullable(request.getMembers()).orElse(List.of())) {
            ms.add(toMemberEntity(mr));
        }
        entity.setMembers(ms);

        List<BusinessUnitConsumer> cs = new ArrayList<>();
        for (BusinessUnitConsumerRequest cr : Optional.ofNullable(request.getConsumers()).orElse(List.of())) {
            cs.add(toConsumerEntity(cr));
        }
        entity.setConsumers(cs);

        return entity;
    }

    private BusinessUnitMember toMemberEntity(BusinessUnitMemberRequest r) {
        if (r == null) {
            return null;
        }
        BusinessUnitMember m = new BusinessUnitMember();
        m.setId(r.getId());
        m.setName(r.getName());
        m.setEmail(r.getEmail());
        return m;
    }


    private BusinessUnitMemberResponse toMemberResponse(BusinessUnitMember m) {
        if (m == null) {
            return null;
        }
        BusinessUnitMemberResponse resp = new BusinessUnitMemberResponse();
        resp.setId(m.getId());
        resp.setName(m.getName());
        resp.setEmail(m.getEmail());
        return resp;
    }

    private BusinessUnitConsumer toConsumerEntity(BusinessUnitConsumerRequest r) {
        if (r == null) {
            return null;
        }
        BusinessUnitConsumer c = new BusinessUnitConsumer();
        c.setId(r.getId());
        c.setConsumerId(r.getConsumerId());
        c.setName(r.getName());
        return c;
    }

    private BusinessUnitConsumerResponse toConsumerResponse(BusinessUnitConsumer c) {
        if (c == null) {
            return null;
        }
        BusinessUnitConsumerResponse resp = new BusinessUnitConsumerResponse();
        resp.setId(c.getId());
        resp.setConsumerId(c.getConsumerId());
        resp.setName(c.getName());
        return resp;
    }
}

