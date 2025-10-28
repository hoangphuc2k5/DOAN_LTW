package manager.service.impl;

import manager.entity.ActivityLog;
import manager.entity.Report;
import manager.entity.User;
import manager.repository.ActivityLogRepository;
import manager.repository.ReportRepository;
import manager.repository.UserRepository;
import manager.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final UserRepository userRepo;
    private final ActivityLogRepository logRepo;

    public ReportServiceImpl(ReportRepository reportRepo, UserRepository userRepo, ActivityLogRepository logRepo) {
        this.reportRepo = reportRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
    }

    @Override
    public Page<Report> listOpenReports(int page, int size) {
        return reportRepo.findByStatus("OPEN", PageRequest.of(page, size));
    }

    @Override
    public void resolveReport(Long reportId, String status, String note, Long managerId) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo ID: " + reportId));

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người xử lý ID: " + managerId));

        report.setStatus(status);
        report.setResolution(note);
        report.setResolvedBy(manager);
        report.setResolvedAt(LocalDateTime.now());
        reportRepo.save(report);

        ActivityLog log = new ActivityLog();
        log.setUser(manager);
        log.setAction("REPORT_RESOLVE");
        log.setEntityType("REPORT");
        log.setEntityId(reportId);
        log.setDetails("Report resolved with status: " + status);
        log.setCreatedAt(LocalDateTime.now());
        logRepo.save(log);
    }

    @Override
    public long countOpenReports() {
        return reportRepo.countByStatus("OPEN");
    }
}
