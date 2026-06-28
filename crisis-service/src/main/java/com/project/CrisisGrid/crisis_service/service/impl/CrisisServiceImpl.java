package com.project.CrisisGrid.crisis_service.service.impl;

import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.entity.CrisisReport;
import com.project.CrisisGrid.crisis_service.exception.CrisisNotFoundException;
import com.project.CrisisGrid.crisis_service.kafka.CrisisEventProducer;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
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

        // Publish event to Kafka triggers AI analysis
        crisisEventProducer.publishCrisisCreatedEvent(saved.getId(), saved.getTitle(), saved.getDescription());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)  // Fix: was `readOnly true` (missing `=`)
    @Cacheable(value = "crisis", key = "#id")  // Fix: wrong import (jakarta.persistence.Cacheable → spring Cacheable)
    public CrisisReportResponse getCrisisById(UUID id) {
        CrisisReport crisis = crisisReportRepository.findById(id)
                .orElseThrow(() -> new CrisisNotFoundException("Crisis not found with id: " + id));

        return mapToResponse(crisis);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrisisReportResponse> getAllActiveCrises(Pageable pageable) {  // Fix: wrong import (java.awt.print.Pageable → spring Pageable)
        return crisisReportRepository
                .findByStatus(CrisisStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @CacheEvict(value = "crisis", key = "#id")
    public CrisisReportResponse updateCrisisStatus(UUID id, CrisisStatus status) {

        CrisisReport crisis = crisisReportRepository.findById(id)
                .orElseThrow(() -> new CrisisNotFoundException("Crisis not found with id: " + id));

        CrisisStatus previousStatus = crisis.getStatus();

        crisis.setStatus(status);

        CrisisReport updated = crisisReportRepository.save(crisis);
        log.info("Crisis {} status changed from {} to {}", id, previousStatus, status);

        // Fix: was `status' CrisisStatus.RESOLVED || status CrisisStatus.CLOSED`
        // Notify via Kafka if crisis is resolved
        if (status == CrisisStatus.RESOLVED || status == CrisisStatus.CLOSED) {
            crisisEventProducer.publishCrisisResolvedEvent(id);
        }

        return mapToResponse(updated);  // Fix: was `map ToResponse` (space in method name)
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrisisReportResponse> getNearbyCrises(Double latitude, Double longitude, Double radiusKm) {

        // Uses Haversine formula via a native query in the repository
        return crisisReportRepository
                .findNearbyCrises(latitude, longitude, radiusKm)  // Fix: was `radiuskm` (wrong case)
                .stream()
                .map(this::mapToResponse)  // Fix: was `map(this::mapToResponse)` (missing dot)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrisisReportResponse> getCrisesByReporter(UUID reportedBy) {  // Fix: was `(` instead of `{`

        return crisisReportRepository
                .findByReportedByOrderByCreatedAtDesc(reportedBy)
                .stream()
                .map(this::mapToResponse)  // Fix: was `map ToResponse` (space in method name)
                .collect(Collectors.toList());
    }  // Fix: closing brace was outside the method

    // Private Mapper
    private CrisisReportResponse mapToResponse(CrisisReport crisis) {  // Fix: was `map ToResponse` (space in method name)
        return CrisisReportResponse.builder()
                .id(crisis.getId())
                .title(crisis.getTitle())
                .description(crisis.getDescription())
                .latitude(crisis.getLatitude())
                .longitude(crisis.getLongitude())
                .address(crisis.getAddress())
                .reportedBy(crisis.getReportedBy())  // Fix: was missing `.` before reportedBy
                .status(crisis.getStatus())
                .crisisType(crisis.getCrisisType())
                .severityScore(crisis.getSeverityScore())  // Fix: was missing `.` before severityScore
                .aiSummary(crisis.getAiSummary())          // Fix: was missing `.` before aiSummary
                .createdAt(crisis.getCreatedAt())
                .updatedAt(crisis.getUpdatedAt())
                .build();
    }
}