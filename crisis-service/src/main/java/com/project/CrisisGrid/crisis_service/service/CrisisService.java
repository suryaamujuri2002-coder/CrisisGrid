package com.project.CrisisGrid.crisis_service.service;



import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.kafka.CrisisEventProducer;
import com.project.CrisisGrid.crisis_service.enums.CrisisStatus;
import com.project.CrisisGrid.crisis_service.repository.CrisisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CrisisService {

    CrisisReportResponse submitCrisis(
            CrisisReportRequest request,
            UUID reportedBy
    );

    CrisisReportResponse getCrisisById(UUID id);

    Page<CrisisReportResponse> getAllActiveCrises(
            Pageable pageable
    );

    CrisisReportResponse updateCrisisStatus(
            UUID id,
            CrisisStatus status
    );

    List<CrisisReportResponse> getNearbyCrises(
            Double latitude,
            Double longitude,
            Double radiusKm
    );

    List<CrisisReportResponse> getCrisesByReporter(
            UUID reportedBy
    );
}