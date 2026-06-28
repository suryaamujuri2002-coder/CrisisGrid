package com.project.CrisisGrid.crisis_service.repository;



import com.project.CrisisGrid.crisis_service.entity.CrisisReport;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CrisisReportRepository
        extends JpaRepository<CrisisReport, UUID> {

    Page<CrisisReport> findByStatus(
            CrisisStatus status,
            Pageable pageable
    );

    List<CrisisReport> findByReportedByOrderByCreatedAtDesc(
            UUID reportedBy
    );

    /**
     * Find crises within a given radius (km)
     * using the Haversine formula.
     */
    @Query(value = """
            SELECT *
            FROM crisis_report
            WHERE status IN ('PENDING', 'ACTIVE')
            AND (
                6371 * acos(
                    cos(radians(:lat))
                    * cos(radians(latitude))
                    * cos(radians(longitude) - radians(:lng))
                    + sin(radians(:lat))
                    * sin(radians(latitude))
                )
            ) <= :radiusKm
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<CrisisReport> findNearbyCrises(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
}