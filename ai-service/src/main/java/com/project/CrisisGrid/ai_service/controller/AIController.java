package com.project.CrisisGrid.ai_service.controller;

import com.project.CrisisGrid.ai_service.agents.AIOrchestrator;
import com.project.CrisisGrid.ai_service.dto.AIAnalysisRequest;
import com.project.CrisisGrid.ai_service.dto.AIAnalysisResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIOrchestrator aiOrchestrator;

    /**
     * Manual trigger for AI analysis
     * (for testing or re-analysis)
     */
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONDER')")
    public ResponseEntity<AIAnalysisResponse> analyzeCrisis(
            @Valid @RequestBody AIAnalysisRequest request) {

        log.info(
                "Manual AI analysis requested for crisis: {}",
                request.getCrisisId()
        );

        AIAnalysisResponse response =
                aiOrchestrator.analyzeCrisis(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {

        return ResponseEntity.ok("AI Service is running");
    }
}
