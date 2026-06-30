package com.project.CrisisGrid.crisis_service.controller;

import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.dto.StatusUpdateRequest;
import com.project.CrisisGrid.crisis_service.security.JwtService;
import com.project.CrisisGrid.crisis_service.service.CrisisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crisis")
@RequiredArgsConstructor
@Slf4j
public class CrisisReportController {

    private final CrisisService crisisService;
    private final JwtService jwtService;

    @PostMapping("/report")
    public ResponseEntity<CrisisReportResponse> submitCrisis(
            @Valid @RequestBody CrisisReportRequest request,
            HttpServletRequest servletRequest) {

        String token = servletRequest.getHeader("Authorization").substring(7);
        UUID reportedBy = UUID.fromString(jwtService.extractUserId(token));
        log.info("Crisis report submitted by user: {}", reportedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(crisisService.submitCrisis(request, reportedBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrisisReportResponse> getCrisisById(@PathVariable UUID id) {
        return ResponseEntity.ok(crisisService.getCrisisById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<CrisisReportResponse>> getAllActiveCrises(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(crisisService.getAllActiveCrises(pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CrisisReportResponse> updateCrisisStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Updating crisis {} status to {}", id, request.getStatus());
        return ResponseEntity.ok(crisisService.updateCrisisStatus(id, request.getStatus()));
    }

    /**
     * BUG 2 FIX — AI analysis write-back endpoint.
     *
     * Receives the AI analysis result from ai-service (via Feign PATCH).
     * Delegates entirely to CrisisService.updateAiAnalysis() which runs
     * inside a @Transactional context and evicts "crisisCache" (the correct
     * registered cache name from CacheConfig.CRISIS_CACHE).
     *
     * Must be permitAll() in SecurityConfig — this is a service-to-service
     * call from ai-service which has no user JWT.
     */
    @PatchMapping("/{id}/ai-analysis")
    public ResponseEntity<Void> updateAiAnalysis(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> analysis) {
        log.info("Received AI analysis update for crisis: {}", id);
        crisisService.updateAiAnalysis(id, analysis);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<CrisisReportResponse>> getNearbyCrises(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        return ResponseEntity.ok(crisisService.getNearbyCrises(lat, lng, radiusKm));
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<CrisisReportResponse>> getMyCrises(
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        UUID reportedBy = UUID.fromString(jwtService.extractUserId(token));
        return ResponseEntity.ok(crisisService.getCrisesByReporter(reportedBy));
    }
}