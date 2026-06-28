package com.project.CrisisGrid.resource_service.repo;

import com.project.CrisisGrid.resource_service.entity.Resource;
import com.project.CrisisGrid.resource_service.enums.ResourceStatus;
import com.project.CrisisGrid.resource_service.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findByStatus(ResourceStatus status);

    List<Resource> findByTypeAndStatus(ResourceType type, ResourceStatus status);

    List<Resource> findByAssignedCrisisId(UUID crisisId);

    @Query(value = """
        SELECT * FROM resource
        WHERE status = 'AVAILABLE'
          AND (:type IS NULL OR type = CAST(:type AS VARCHAR))
          AND current_latitude IS NOT NULL
          AND current_longitude IS NOT NULL
          AND (
                6371 * acos(
                    cos(radians(:lat))
                    * cos(radians(current_latitude))
                    * cos(radians(current_longitude) - radians(:lng))
                    + sin(radians(:lat))
                    * sin(radians(current_latitude))
                )
              ) <= :radiuskm
        ORDER BY (
                6371 * acos(
                    cos(radians(:lat))
                    * cos(radians(current_latitude))
                    * cos(radians(current_longitude) - radians(:lng))
                    + sin(radians(:lat))
                    * sin(radians(current_latitude))
                )
        ) ASC
        """, nativeQuery = true)
    List<Resource> findNearbyAvailableResources(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiuskm") Double radiuskm,
            @Param("type") String type
    );

    Long countByStatus(ResourceStatus status);

    Long countByTypeAndStatus(ResourceType type, ResourceStatus status);
}