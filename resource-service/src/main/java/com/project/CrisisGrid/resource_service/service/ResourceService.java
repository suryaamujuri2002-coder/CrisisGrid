package com.project.CrisisGrid.resource_service.service;

import com.project.CrisisGrid.resource_service.dto.*;

import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface ResourceService {

    ResourceResponse createResource(ResourceRequest request);

    ResourceResponse getResourceById(UUID id);

    Page<ResourceResponse> getAllResources(Pageable pageable);

    List<ResourceResponse> getResourcesByStatus(ResourceStatus status);

    List<ResourceResponse> getAvailableResourcesByType(ResourceType type);

    ResourceResponse updateResource(UUID id, ResourceRequest request);

    void deleteResource(UUID id);

    List<AllocationResponse> allocateResources(
            AllocationRequest request,
            UUID allocatedBy
    );

    void releaseResource(UUID resourceId);

    void releaseAllResourcesForCrisis(UUID crisisId);

    List<AllocationResponse> getAllocationsByCrisis(UUID crisisId);

    List<ResourceResponse> getNearbyResources(
            NearbyResourceRequest request
    );

    ResourceResponse updateResourceLocation(
            UUID id,
            Double latitude,
            Double longitude
    );
}