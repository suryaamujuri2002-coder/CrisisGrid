package com.project.CrisisGrid.resource_service.controller;

import com.project.CrisisGrid.resource_service.dto.*;
import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import com.project.CrisisGrid.resource_service.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;

    // Create a new resource (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestBody ResourceRequest request) {

        log.info("Creating new resource: {}", request.getName());

        ResourceResponse response = resourceService.createResource(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get resource by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable UUID id) {
        System.out.println("Controller reached");
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    // Get all resources (paginated)
    @GetMapping
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(resourceService.getAllResources(pageable));
    }

    // Get available resources
    @GetMapping("/available")
    public ResponseEntity<List<ResourceResponse>> getAvailableResources() {

        return ResponseEntity.ok(
                resourceService.getResourcesByStatus(ResourceStatus.AVAILABLE)
        );
    }

    // Get deployed resources
    @GetMapping("/deployed")
    public ResponseEntity<List<ResourceResponse>> getDeployedResources() {

        return ResponseEntity.ok(
                resourceService.getResourcesByStatus(ResourceStatus.DEPLOYED)
        );
    }

    // Get available resources by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ResourceResponse>> getResourcesByType(
            @PathVariable ResourceType type) {

        return ResponseEntity.ok(
                resourceService.getAvailableResourcesByType(type)
        );
    }

    // Update resource (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable UUID id,
            @Valid @RequestBody ResourceRequest request) {

        log.info("Updating resource {}", id);

        return ResponseEntity.ok(
                resourceService.updateResource(id, request)
        );
    }

    // Delete resource (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResource(
            @PathVariable UUID id) {

        log.info("Deleting resource {}", id);

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }

    // Allocate resources to a crisis
    @PostMapping("/allocate")
    @PreAuthorize("hasAnyRole('RESPONDER','ADMIN')")
    public ResponseEntity<List<AllocationResponse>> allocateResources(
            @Valid @RequestBody AllocationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        log.info(
                "Allocating {} resources to crisis {}",
                request.getResourceIds().size(),
                request.getCrisisId()
        );

        List<AllocationResponse> allocations =
                resourceService.allocateResources(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(allocations);
    }

    // Release all resources for a crisis
    @PostMapping("/release/crisis/{crisisId}")
    @PreAuthorize("hasAnyRole('RESPONDER','ADMIN')")
    public ResponseEntity<Void> releaseAllResourcesForCrisis(
            @PathVariable UUID crisisId) {

        log.info(
                "Releasing all resources for crisis {}",
                crisisId
        );

        resourceService.releaseAllResourcesForCrisis(crisisId);

        return ResponseEntity.noContent().build();
    }

    // Get allocation history for a crisis
    @GetMapping("/allocations/crisis/{crisisId}")
    public ResponseEntity<List<AllocationResponse>> getAllocationsByCrisis(
            @PathVariable UUID crisisId) {

        return ResponseEntity.ok(
                resourceService.getAllocationsByCrisis(crisisId)
        );
    }

    // Find nearby available resources
    @PostMapping("/nearby")
    public ResponseEntity<List<ResourceResponse>> getNearbyResources(
            @Valid @RequestBody NearbyResourceRequest request) {

        log.info(
                "Searching for resources near lat={}, lng={}, radius={}km",
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusKm()
        );

        return ResponseEntity.ok(
                resourceService.getNearbyResources(request)
        );
    }

    // Update resource location
    @PatchMapping("/{id}/location")
    public ResponseEntity<ResourceResponse> updateResourceLocation(
            @PathVariable UUID id,
            @RequestParam Double lat,
            @RequestParam Double lng) {

        log.info(
                "Updating location for resource {} to ({}, {})",
                id,
                lat,
                lng
        );

        return ResponseEntity.ok(
                resourceService.updateResourceLocation(id, lat, lng)
        );
    }
}