package com.project.CrisisGrid.crisis_service.controller;


import com.project.CrisisGrid.crisis_service.dto.CrisisReportRequest;
import com.project.CrisisGrid.crisis_service.dto.CrisisReportResponse;
import com.project.CrisisGrid.crisis_service.dto.StatusUpdateRequest;
import com.project.CrisisGrid.crisis_service.security.JwtService;
import com.project.CrisisGrid.crisis_service.service.CrisisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crisis")
@RequiredArgsConstructor
@Slf4j
public class CrisisReportController {

    private final CrisisService crisisService;
    private final JwtService jwtService;

    /**
     * Submit a new crisis report.
     * Authenticated users only.
     */
    @PostMapping("/report")
    public ResponseEntity<CrisisReportResponse> submitCrisis(
            @Valid @RequestBody CrisisReportRequest request,
            HttpServletRequest servletRequest) {

        String authHeader =
                servletRequest.getHeader("Authorization");

        String token =
                authHeader.substring(7);

        UUID reportedBy =
                UUID.fromString(
                        jwtService.extractUserId(token)
                );

        log.info("Crisis report submitted by user: {}", reportedBy);

        CrisisReportResponse response =
                crisisService.submitCrisis(
                        request,
                        reportedBy
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrisisReportResponse> getCrisisById(
            @PathVariable UUID id) {

        log.info("Fetching crisis with id: {}", id);

        return ResponseEntity.ok(
                crisisService.getCrisisById(id)
        );
    }

    /**
     * Get paginated list of all active crises.
     */
    @GetMapping("/active")
    public ResponseEntity<Page<CrisisReportResponse>> getAllActiveCrises(
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable) {

        return ResponseEntity.ok(
                crisisService.getAllActiveCrises(pageable)
        );
    }

    /**
     * Update crisis status (ACTIVE, RESOLVED, CLOSED).
     * Admin/Responder role only.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CrisisReportResponse> updateCrisisStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {

        log.info(
                "Updating crisis {} status to {}",
                id,
                request.getStatus()
        );

        CrisisReportResponse response =
                crisisService.updateCrisisStatus(
                        id,
                        request.getStatus()
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Get crises near a given location within a radius.
     * Default radius: 10 km.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<CrisisReportResponse>> getNearbyCrises(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {

        log.info(
                "Fetching crises near lat={}, lng={}, radius={}km",
                lat,
                lng,
                radiusKm
        );

        return ResponseEntity.ok(
                crisisService.getNearbyCrises(
                        lat,
                        lng,
                        radiusKm
                )
        );
    }

    /**
     * Get all crises reported by the authenticated user.
     */
    @GetMapping("/my-reports")
    public ResponseEntity<List<CrisisReportResponse>> getMyCrises(
            HttpServletRequest request) {

        String authHeader =
                request.getHeader("Authorization");

        String token =
                authHeader.substring(7);

        UUID reportedBy =
                UUID.fromString(
                        jwtService.extractUserId(token)
                );

        return ResponseEntity.ok(
                crisisService.getCrisesByReporter(reportedBy)
        );
    }
}