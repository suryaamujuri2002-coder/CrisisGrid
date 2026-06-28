package com.project.CrisisGrid.resource_service.service;

import com.project.CrisisGrid.resource_service.dto.*;
import com.project.CrisisGrid.resource_service.entity.Resource;
import com.project.CrisisGrid.resource_service.entity.ResourceAllocation;
import com.project.CrisisGrid.resource_service.enums.AllocationType;
import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import com.project.CrisisGrid.resource_service.kafka.ResourceEventProducer;
import com.project.CrisisGrid.resource_service.repo.ResourceAllocationRepository;
import com.project.CrisisGrid.resource_service.repo.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceEventProducer resourceEventProducer;

    @Override
    public ResourceResponse createResource(ResourceRequest request) {

        Resource resource = Resource.builder()
                .name(request.getName())
                .type(request.getType())
                .status(request.getStatus())
                .currentLatitude(request.getCurrentLatitude())
                .currentLongitude(request.getCurrentLongitude())
                .baseLocation(request.getBaseLocation())
                .capacity(request.getCapacity())
                .contactNumber(request.getContactNumber())
                .description(request.getDescription())
                .build();

        Resource saved = resourceRepository.save(resource);

        log.info("Resource created {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "resource", key = "#id")
    public ResourceResponse getResourceById(UUID id) {

        return mapToResponse(findResourceOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResourceResponse> getAllResources(Pageable pageable) {

        return resourceRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourcesByStatus(ResourceStatus status) {

        return resourceRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAvailableResourcesByType(ResourceType type) {

        return resourceRepository
                .findByTypeAndStatus(type, ResourceStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "resource", key = "#id")
    public ResourceResponse updateResource(UUID id,
                                           ResourceRequest request) {

        Resource resource = findResourceOrThrow(id);

        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setStatus(request.getStatus());
        resource.setCurrentLatitude(request.getCurrentLatitude());
        resource.setCurrentLongitude(request.getCurrentLongitude());
        resource.setBaseLocation(request.getBaseLocation());
        resource.setCapacity(request.getCapacity());
        resource.setContactNumber(request.getContactNumber());
        resource.setDescription(request.getDescription());

        Resource updated = resourceRepository.save(resource);

        log.info("Updated resource {}", id);

        return mapToResponse(updated);
    }

    @Override
    @CacheEvict(value = "resource", key = "#id")
    public void deleteResource(UUID id) {

        Resource resource = findResourceOrThrow(id);

        if (resource.getStatus() == ResourceStatus.DEPLOYED) {
            throw new IllegalStateException(
                    "Cannot delete deployed resource."
            );
        }

        resourceRepository.delete(resource);

        log.info("Deleted resource {}", id);
    }

    private Resource findResourceOrThrow(UUID id) {

        return resourceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Resource not found : " + id));
    }

    private ResourceResponse mapToResponse(Resource resource) {

        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .status(resource.getStatus())
                .currentLatitude(resource.getCurrentLatitude())
                .currentLongitude(resource.getCurrentLongitude())
                .baseLocation(resource.getBaseLocation())
                .capacity(resource.getCapacity())
                .assignedCrisisId(resource.getAssignedCrisisId())
                .contactNumber(resource.getContactNumber())
                .description(resource.getDescription())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    @Override
    public List<AllocationResponse> allocateResources(
            AllocationRequest request,
            UUID allocatedBy) {

        List<AllocationResponse> responses = new ArrayList<>();

        for (UUID resourceId : request.getResourceIds()) {

            Resource resource = findResourceOrThrow(resourceId);

            if (resource.getStatus() != ResourceStatus.AVAILABLE) {
                throw new IllegalStateException(
                        "Resource " + resourceId + " is not available."
                );
            }

            resource.setStatus(ResourceStatus.DEPLOYED);
            resource.setAssignedCrisisId(request.getCrisisId());

            resourceRepository.save(resource);

            ResourceAllocation allocation = ResourceAllocation.builder()
                    .crisisId(request.getCrisisId())
                    .resourceId(resourceId)
                    .allocatedBy(AllocationType.MANUAL)
                    .allocatedByUserId(allocatedBy)
                    .notes(request.getNotes())
                    .build();

            allocation = allocationRepository.save(allocation);

            responses.add(mapAllocationResponse(allocation, resource));
        }

        resourceEventProducer.publishResourceAllocatedEvent(
                request.getCrisisId(),
                request.getResourceIds()
        );

        log.info("Allocated {} resources for crisis {}",
                request.getResourceIds().size(),
                request.getCrisisId());

        return responses;
    }

    @Override
    public void releaseResource(UUID resourceId) {

        Resource resource = findResourceOrThrow(resourceId);

        ResourceAllocation allocation = allocationRepository
                .findActiveByResourceId(resourceId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active allocation not found."
                        ));

        allocation.setReleasedAt(LocalDateTime.now());

        allocationRepository.save(allocation);

        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setAssignedCrisisId(null);

        resourceRepository.save(resource);

        resourceEventProducer.publishResourceReleasedEvent(resourceId);

        log.info("Released resource {}", resourceId);
    }

    @Override
    public void releaseAllResourcesForCrisis(UUID crisisId) {

        List<ResourceAllocation> allocations =
                allocationRepository.findActiveByCrisisId(crisisId);

        for (ResourceAllocation allocation : allocations) {

            Resource resource =
                    findResourceOrThrow(allocation.getResourceId());

            allocation.setReleasedAt(LocalDateTime.now());

            allocationRepository.save(allocation);

            resource.setStatus(ResourceStatus.AVAILABLE);
            resource.setAssignedCrisisId(null);

            resourceRepository.save(resource);

            resourceEventProducer.publishResourceReleasedEvent(
                    resource.getId()
            );
        }

        log.info("Released all resources for crisis {}", crisisId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByCrisis(UUID crisisId) {

        return allocationRepository
                .findByCrisisIdOrderByAllocatedAtDesc(crisisId)
                .stream()
                .map(allocation -> {

                    Resource resource =
                            findResourceOrThrow(allocation.getResourceId());

                    return mapAllocationResponse(allocation, resource);

                })
                .collect(Collectors.toList());
    }

    private AllocationResponse mapAllocationResponse(
            ResourceAllocation allocation,
            Resource resource) {

        return AllocationResponse.builder()
                .id(allocation.getId())
                .crisisId(allocation.getCrisisId())
                .resourceId(allocation.getResourceId())
                .resourceName(resource.getName())
                .allocatedAt(allocation.getAllocatedAt())
                .releasedAt(allocation.getReleasedAt())
                .allocatedBy(allocation.getAllocatedBy())
                .notes(allocation.getNotes())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getNearbyResources(
            NearbyResourceRequest request) {

        String type = request.getResourceType() == null
                ? null
                : request.getResourceType().name();

        return resourceRepository.findNearbyAvailableResources(
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getRadiusKm(),
                        type
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "resource", key = "#id")
    public ResourceResponse updateResourceLocation(
            UUID id,
            Double latitude,
            Double longitude) {

        Resource resource = findResourceOrThrow(id);

        resource.setCurrentLatitude(latitude);
        resource.setCurrentLongitude(longitude);

        Resource updated = resourceRepository.save(resource);

        log.info(
                "Updated location for resource {} to ({}, {})",
                id,
                latitude,
                longitude
        );

        return mapToResponse(updated);
    }

}