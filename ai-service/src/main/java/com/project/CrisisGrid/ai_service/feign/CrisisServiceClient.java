package com.project.CrisisGrid.ai_service.feign;




import com.project.CrisisGrid.ai_service.dto.CrisisDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "crisis-service", url = "${crisis-service.url}")
public interface CrisisServiceClient {

    @GetMapping("/api/v1/crisis/{id}")
    CrisisDTO getCrisisById(@PathVariable UUID id);

    @PatchMapping("/api/v1/crisis/{id}/ai-analysis")
    void updateCrisisWithAIAnalysis(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> analysis
    );

}