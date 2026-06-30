package com.project.CrisisGrid.crisis_service.repository;

import com.project.CrisisGrid.crisis_service.entity.CrisisReport;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
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
     * Find crises within a given radius (km) using the Haversine formula.
     *
     * FIX — "ERROR: input is out of range" from PostgreSQL's acos().
     *
     * The original query passed the raw dot-product value directly to acos().
     * Due to floating-point rounding, this value can drift slightly outside
     * [-1.0, 1.0] (e.g. 1.0000000002) when the query point is very close to
     * or exactly on a stored coordinate. PostgreSQL's acos() throws an error
     * for any value outside that range instead of clamping silently.
     *
     * Fix: wrap the dot-product in GREATEST(-1.0, LEAST(1.0, ...)) before
     * passing it to acos(). This clamps any floating-point overshoot back
     * into the valid domain without affecting results for normal coordinates.
     */
    @Query(value = """
            SELECT *
            FROM crisis_report
            WHERE status IN ('PENDING', 'ACTIVE')
            AND (
                6371 * acos(
                    GREATEST(-1.0, LEAST(1.0,
                        cos(radians(:lat))
                        * cos(radians(latitude))
                        * cos(radians(longitude) - radians(:lng))
                        + sin(radians(:lat))
                        * sin(radians(latitude))
                    ))
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