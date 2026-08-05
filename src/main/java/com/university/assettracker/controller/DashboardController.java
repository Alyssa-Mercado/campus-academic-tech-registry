package com.university.assettracker.controller;

import com.university.assettracker.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("totalAssets", dashboardService.totalAssets());
        model.addAttribute("countByType", dashboardService.countByType());
        model.addAttribute("upcomingCount", dashboardService.upcomingMaintenanceCount());
        model.addAttribute("overdueCount", dashboardService.overdueMaintenanceCount());
        model.addAttribute("expiringSoonCount", dashboardService.expiringSoonCount());
        model.addAttribute("expiredCount", dashboardService.expiredWarrantyCount());
        model.addAttribute("recommendationCount", dashboardService.recommendationCount());
        return "dashboard";
    }
}
