package com.stackoverflow.service.common;

import com.stackoverflow.entity.Report;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Report Service - Quản lý báo cáo vi phạm
 */
@Service
@Transactional
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    /**
     * Tạo báo cáo mới
     */
    public Report createReport(User reporter, String entityType, Long entityId, String reason, String description) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setEntityType(entityType);
        report.setEntityId(entityId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    /**
     * Lấy báo cáo của user
     */
    public Page<Report> getReportsByUser(User user, Pageable pageable) {
        return reportRepository.findByReporter(user, pageable);
    }

    /**
     * Lấy tất cả báo cáo (Admin)
     */
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    /**
     * Lấy báo cáo theo trạng thái
     */
    public Page<Report> getReportsByStatus(String status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable);
    }

    /**
     * Xử lý báo cáo
     */
    public void resolveReport(Long reportId, User resolver, String resolution) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus("RESOLVED");
        report.setResolvedBy(resolver);
        report.setResolution(resolution);
        report.setResolvedAt(LocalDateTime.now());
        
        reportRepository.save(report);
    }

    /**
     * Từ chối báo cáo
     */
    public void rejectReport(Long reportId, User resolver, String reason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus("REJECTED");
        report.setResolvedBy(resolver);
        report.setResolution(reason);
        report.setResolvedAt(LocalDateTime.now());
        
        reportRepository.save(report);
    }

    /**
     * Thống kê
     */
    public long countPending() {
        return reportRepository.countByStatus("PENDING");
    }

    public long countResolved() {
        return reportRepository.countByStatus("RESOLVED");
    }

    public long countAll() {
        return reportRepository.count();
    }
}

