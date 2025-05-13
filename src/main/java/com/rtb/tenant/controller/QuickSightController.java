package com.rtb.tenant.controller;

import com.rtb.tenant.dto.CategoryResponse;
import com.rtb.core.entity.tenant.QuickSightDashboard;
import com.rtb.tenant.service.QuickSightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("api/v1/tenants/insights")
public class QuickSightController {

    @Autowired
    private QuickSightService quickSightService;

    @GetMapping("/{categoryName}/{reportName}") // Tenant and Platform
    @PreAuthorize("hasAuthority('insights_read')")
    public ResponseEntity<Map<String, String>> getEmbedUrl(
            @PathVariable String categoryName,
            @PathVariable String reportName) {

         String embedUrl = quickSightService.generateEmbedUrl(categoryName, reportName);

        Map<String, String> response = new HashMap<>();
        if (embedUrl.startsWith("Error")) {
            response.put("error", embedUrl);
            return ResponseEntity.badRequest().body(response);
        } else {
            response.put("embedUrl", embedUrl);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/categories") //Tenant and platform
    @PreAuthorize("hasAuthority('insights_read')")
    public ResponseEntity<List<CategoryResponse>> getReportsGroupedByCategory() {
        // Fetch all reports
        List<QuickSightDashboard> reports = quickSightService.getAllReports();

        // Group by category and map to the desired response format
        List<CategoryResponse> response = reports.stream()
                .collect(Collectors.groupingBy(QuickSightDashboard::getCategory))
                .entrySet()
                .stream()
                .map(entry -> new CategoryResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


}
