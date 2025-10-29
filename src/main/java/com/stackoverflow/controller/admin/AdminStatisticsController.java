package com.stackoverflow.controller.admin;

import com.stackoverflow.service.common.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Statistics Controller - Thống kê báo cáo
 */
@Controller
@RequestMapping("/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Dashboard thống kê
     */
    @GetMapping
    public String statisticsDashboard(Model model) {
        
        var overview = statisticsService.getOverviewStatistics();
        var timeStats = statisticsService.getTimeBasedStatistics();
        
        model.addAllAttributes(overview);
        model.addAllAttributes(timeStats);
        
        model.addAttribute("pageTitle", "Statistics & Reports - Admin");
        
        return "admin/statistics/dashboard";
    }
}

