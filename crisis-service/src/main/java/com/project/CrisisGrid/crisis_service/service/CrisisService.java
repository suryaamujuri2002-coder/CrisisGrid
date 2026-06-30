package com.project.CrisisGrid.crisis_service.service;

import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CrisisService {

    CrisisReportResponse submitCrisis(CrisisReportRequest request, UUID reportedBy);

    CrisisReportResponse getCrisisById(UUID id);

    Page<CrisisReportResponse> getAllActiveCrises(Pageable pageable);

    CrisisReportResponse updateCrisisStatus(UUID id, CrisisStatus status);

    /**
     * Called by CrisisReportController when ai-service PATCHes back
     * its analysis result (crisisType, severityScore, aiSummary, status).
     * Runs transactionally and evicts the correct "crisisCache" entry.
     */
    void updateAiAnalysis(UUID id, Map<String, Object> analysis);

    List<CrisisReportResponse> getNearbyCrises(Double latitude, Double longitude, Double radiusKm);

    List<CrisisReportResponse> getCrisesByReporter(UUID reportedBy);
}