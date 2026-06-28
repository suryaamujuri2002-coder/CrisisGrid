package com.project.CrisisGrid.resource_service.repo;

import com.project.CrisisGrid.resource_service.entity.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, UUID> {

    List<ResourceAllocation> findByCrisisIdOrderByAllocatedAtDesc(UUID crisisId);

    List<ResourceAllocation> findByResourceIdOrderByAllocatedAtDesc(UUID resourceId);

    @Query("""
        SELECT ra
        FROM ResourceAllocation ra
        WHERE ra.crisisId = :crisisId
          AND ra.releasedAt IS NULL
    """)
    List<ResourceAllocation> findActiveByCrisisId(
            @Param("crisisId") UUID crisisId
    );

    @Query("""
        SELECT ra
        FROM ResourceAllocation ra
        WHERE ra.resourceId = :resourceId
          AND ra.releasedAt IS NULL
    """)
    Optional<ResourceAllocation> findActiveByResourceId(
            @Param("resourceId") UUID resourceId
    );
}
