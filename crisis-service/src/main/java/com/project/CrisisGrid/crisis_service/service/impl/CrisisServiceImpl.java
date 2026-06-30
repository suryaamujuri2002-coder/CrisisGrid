package com.project.CrisisGrid.crisis_service.service.impl;

import com.project.CrisisGrid.crisis_service.config.CacheConfig;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.entity.CrisisReport;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import com.project.CrisisGrid.crisis_service.enums.CrisisType;
import com.project.CrisisGrid.crisis_service.exception.CrisisNotFoundException;
import com.project.CrisisGrid.crisis_service.kafka.CrisisEventProducer;
import com.project.CrisisGrid.crisis_service.repository.CrisisReportRepository;
import com.project.CrisisGrid.crisis_service.service.CrisisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrisisServiceImpl implements CrisisService {

    private final CrisisReportRepository crisisReportRepository;
    private final CrisisEventProducer crisisEventProducer;

    @Override
    public CrisisReportResponse submitCrisis(CrisisReportRequest request, UUID reportedBy) {

        CrisisReport crisis = CrisisReport.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .reportedBy(reportedBy)
                .crisisType(request.getCrisisType())
                .status(CrisisStatus.PENDING)
                .build();

        CrisisReport saved = crisisReportRepository.save(crisis);
        log.info("Crisis saved with id: {}", saved.getId());

        crisisEventProducer.publishCrisisCreatedEvent(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getLatitude(),
                saved.getLongitude()
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CRISIS_CACHE, key = "#id")
    public CrisisReportResponse getCrisisById(UUID id) {
        CrisisReport crisis = crisisReportRepository.findById(id)
                .orElseThrow(() -> new CrisisNotFoundException("Crisis not found with id: " + id));
        return mapToResponse(crisis);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrisisReportResponse> getAllActiveCrises(Pageable pageable) {
        return crisisReportRepository
                .findByStatus(CrisisStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @CacheEvict(value = CacheConfig.CRISIS_CACHE, key = "#id")
    public CrisisReportResponse updateCrisisStatus(UUID id, CrisisStatus status) {

        CrisisReport crisis = crisisReportRepository.findById(id)
                .orElseThrow(() -> new CrisisNotFoundException("Crisis not found with id: " + id));

        CrisisStatus previousStatus = crisis.getStatus();
        crisis.setStatus(status);

        CrisisReport updated = crisisReportRepository.save(crisis);
        log.info("Crisis {} status changed from {} to {}", id, previousStatus, status);

        if (status == CrisisStatus.RESOLVED || status == CrisisStatus.CLOSED) {
            crisisEventProducer.publishCrisisResolvedEvent(id);
        }

        return mapToResponse(updated);
    }

    /**
     * FIX — AI analysis write-back.
     *
     * Previous version of CrisisReportController tried to do this directly
     * in the controller, which caused two problems:
     *
     * 1. @CacheEvict(value = "crisis", ...) — wrong cache name.
     *    The registered cache is "crisisCache" (CacheConfig.CRISIS_CACHE),
     *    not "crisis". Spring silently ignores eviction of an unknown cache,
     *    so the old cached value would be served even after the AI update.
     *
     * 2. Repository injected into Controller — bypasses the @Transactional
     *    boundary on the service layer, meaning the save could fail without
     *    a proper rollback, and the @CacheEvict AOP proxy didn't apply.
     *
     * This method runs inside the existing @Transactional class, uses the
     * correct CacheConfig.CRISIS_CACHE constant, and handles all type
     * conversions safely.
     */
    @Override
    @CacheEvict(value = CacheConfig.CRISIS_CACHE, key = "#id")
    public void updateAiAnalysis(UUID id, Map<String, Object> analysis) {

        CrisisReport crisis = crisisReportRepository.findById(id)
                .orElseThrow(() -> new CrisisNotFoundException("Crisis not found with id: " + id));

        if (analysis.containsKey("crisisType") && analysis.get("crisisType") != null) {
            try {
                crisis.setCrisisType(CrisisType.valueOf((String) analysis.get("crisisType")));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown crisisType from AI: {}", analysis.get("crisisType"));
            }
        }

        if (analysis.containsKey("severityScore") && analysis.get("severityScore") != null) {
            crisis.setSeverityScore(((Number) analysis.get("severityScore")).intValue());
        }

        if (analysis.containsKey("aiSummary") && analysis.get("aiSummary") != null) {
            crisis.setAiSummary((String) analysis.get("aiSummary"));
        }

        // AI upgrades PENDING crises to ACTIVE after scoring
        if (analysis.containsKey("status") && analysis.get("status") != null) {
            try {
                crisis.setStatus(CrisisStatus.valueOf((String) analysis.get("status")));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown status from AI: {}", analysis.get("status"));
            }
        }

        crisisReportRepository.save(crisis);

        log.info("AI analysis saved for crisis {} — severity={}, type={}, status={}",
                id, crisis.getSeverityScore(), crisis.getCrisisType(), crisis.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrisisReportResponse> getNearbyCrises(Double latitude, Double longitude, Double radiusKm) {
        return crisisReportRepository
                .findNearbyCrises(latitude, longitude, radiusKm)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrisisReportResponse> getCrisesByReporter(UUID reportedBy) {
        return crisisReportRepository
                .findByReportedByOrderByCreatedAtDesc(reportedBy)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CrisisReportResponse mapToResponse(CrisisReport crisis) {
        return CrisisReportResponse.builder()
                .id(crisis.getId())
                .title(crisis.getTitle())
                .description(crisis.getDescription())
                .latitude(crisis.getLatitude())
                .longitude(crisis.getLongitude())
                .address(crisis.getAddress())
                .reportedBy(crisis.getReportedBy())
                .status(crisis.getStatus())
                .crisisType(crisis.getCrisisType())
                .severityScore(crisis.getSeverityScore())
                .aiSummary(crisis.getAiSummary())
                .createdAt(crisis.getCreatedAt())
                .updatedAt(crisis.getUpdatedAt())
                .build();
    }
}