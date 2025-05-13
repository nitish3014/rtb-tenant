package com.rtb.tenant.dto;

import com.rtb.core.entity.tenant.QuickSightDashboard;

import java.util.List;

public class CategoryResponse {
    private String category;
    private List<QuickSightDashboard> reports;

    public CategoryResponse(String category, List<QuickSightDashboard> reports) {
        this.category = category;
        this.reports = reports;
    }

    // Getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<QuickSightDashboard> getReports() {
        return reports;
    }

    public void setReports(List<QuickSightDashboard> reports) {
        this.reports = reports;
    }
}
