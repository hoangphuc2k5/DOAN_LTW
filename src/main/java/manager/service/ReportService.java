package manager.service;

import manager.entity.Report;
import org.springframework.data.domain.Page;

public interface ReportService {
    Page<Report> listOpenReports(int page, int size);
    void resolveReport(Long reportId, String status, String note, Long managerId);
    long countOpenReports();
}
