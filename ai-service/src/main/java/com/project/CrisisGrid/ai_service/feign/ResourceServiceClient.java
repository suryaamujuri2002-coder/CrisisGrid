package com.project.CrisisGrid.ai_service.feign;


import com.project.CrisisGrid.ai_service.dto.ResourceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "resource-service", url = "${resource-service.url}")
public interface ResourceServiceClient {

    @PostMapping("/api/v1/resources/nearby")
    List<ResourceDTO> getNearbyResources(
            @RequestBody Map<String, Object> request
    );

    @PostMapping("/api/v1/resources/allocate")
    void allocateResources(
            @RequestBody Map<String, Object> allocationRequest
    );

}
