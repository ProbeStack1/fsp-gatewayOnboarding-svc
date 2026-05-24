package com.probestack.forgesphere.onboarding.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.probestack.forgesphere.onboarding.dto.ApiResponse;


import com.probestack.forgesphere.onboarding.constant.AppConstants;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitConsumerRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitConsumerResponse;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitMemberRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitMemberResponse;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitRequest;
import com.probestack.forgesphere.onboarding.dto.BusinessUnitResponse;
import com.probestack.forgesphere.onboarding.service.BusinessUnitCollectionService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1" + AppConstants.BUSINESS_UNIT_PATH)
public class BusinessUnitController {


    private final BusinessUnitCollectionService service;

    public BusinessUnitController(BusinessUnitCollectionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessUnitResponse>> submit(@RequestBody BusinessUnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business unit submitted successfully", service.createOrSubmit(request)));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessUnitResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Business units fetched successfully", service.getAll()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessUnitResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Business unit fetched successfully", service.getById(id)));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessUnitResponse>> update(@PathVariable String id, @RequestBody BusinessUnitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Business unit updated successfully", service.update(id, request)));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Business unit deleted successfully", null));
    }


    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<BusinessUnitMemberResponse>>> getMembers(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Business unit members fetched successfully", service.getMembers(id)));
    }


    @PutMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<BusinessUnitMemberResponse>>> putMembers(
            @PathVariable String id,
            @RequestBody List<BusinessUnitMemberRequest> members) {
        return ResponseEntity.ok(ApiResponse.success("Business unit members updated successfully", service.upsertMembers(id, members)));
    }


    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable String id, @PathVariable String memberId) {
        service.deleteMember(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Business unit member deleted successfully", null));
    }


    @GetMapping("/{id}/consumers")
    public ResponseEntity<ApiResponse<List<BusinessUnitConsumerResponse>>> getConsumers(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Business unit consumers fetched successfully", service.getConsumers(id)));
    }


    @PutMapping("/{id}/consumers")
    public ResponseEntity<ApiResponse<List<BusinessUnitConsumerResponse>>> putConsumers(
            @PathVariable String id,
            @RequestBody List<BusinessUnitConsumerRequest> consumers) {
        return ResponseEntity.ok(ApiResponse.success("Business unit consumers updated successfully", service.upsertConsumers(id, consumers)));
    }


    @DeleteMapping("/{id}/consumers/{consumerRecordId}")
    public ResponseEntity<ApiResponse<Void>> deleteConsumer(
            @PathVariable String id,
            @PathVariable String consumerRecordId) {
        service.deleteConsumer(id, consumerRecordId);
        return ResponseEntity.ok(ApiResponse.success("Business unit consumer deleted successfully", null));
    }

}

