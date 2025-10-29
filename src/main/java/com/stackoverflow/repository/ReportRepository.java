package com.stackoverflow.repository;

import com.stackoverflow.entity.Report;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    Page<Report> findByReporter(User reporter, Pageable pageable);
    
    Page<Report> findByStatus(String status, Pageable pageable);
    
    long countByStatus(String status);
    
    // For user deletion
    void deleteByReporter(User reporter);
}
