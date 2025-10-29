package com.stackoverflow.service.common;

import com.stackoverflow.entity.SystemSettings;
import com.stackoverflow.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * System Settings Service - Quản lý cài đặt hệ thống
 */
@Service
@Transactional
public class SystemSettingsService {

    @Autowired
    private SystemSettingsRepository repository;

    public SystemSettings getSettings() {
        return repository.findAll().stream().findFirst()
                .orElseGet(() -> createDefaultSettings());
    }

    public SystemSettings updateSettings(SystemSettings settings) {
        return repository.save(settings);
    }

    private SystemSettings createDefaultSettings() {
        SystemSettings settings = new SystemSettings();
        settings.setSiteName("StackOverflow Clone");
        settings.setSiteDescription("A Q&A Platform");
        settings.setContactEmail("admin@stackoverflow.com");
        settings.setLanguage("en");
        settings.setTheme("light");
        settings.setEnableRegistration(true);
        settings.setEnableComments(true);
        settings.setRequireEmailVerification(false);
        settings.setRequireModeration(false);
        settings.setMaxUploadSize(10L);
        settings.setPointsPerQuestion(10);
        settings.setPointsPerAnswer(15);
        settings.setPointsPerAcceptedAnswer(25);
        
        return repository.save(settings);
    }

    public void setSetting(String key, String value) {
        SystemSettings settings = getSettings();
        
        switch (key) {
            case "siteName" -> settings.setSiteName(value);
            case "siteDescription" -> settings.setSiteDescription(value);
            case "contactEmail" -> settings.setContactEmail(value);
            case "language" -> settings.setLanguage(value);
            case "theme" -> settings.setTheme(value);
        }
        
        repository.save(settings);
    }

    /**
     * Upload logo file
     */
    public String uploadLogo(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Create uploads directory if not exists
        Path uploadPath = Paths.get("uploads/logos");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update settings
        SystemSettings settings = getSettings();
        settings.setLogoPath("/uploads/logos/" + filename);
        repository.save(settings);

        return "/uploads/logos/" + filename;
    }

    /**
     * Upload favicon
     */
    public String uploadFavicon(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Create uploads directory
        Path uploadPath = Paths.get("uploads/logos");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "favicon-" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update settings
        SystemSettings settings = getSettings();
        settings.setFaviconPath("/uploads/logos/" + filename);
        repository.save(settings);

        return "/uploads/logos/" + filename;
    }

    /**
     * Update theme colors
     */
    public void updateThemeColors(String primary, String secondary, String accent) {
        SystemSettings settings = getSettings();
        
        if (primary != null && !primary.isEmpty()) {
            settings.setPrimaryColor(primary);
        }
        if (secondary != null && !secondary.isEmpty()) {
            settings.setSecondaryColor(secondary);
        }
        if (accent != null && !accent.isEmpty()) {
            settings.setAccentColor(accent);
        }
        
        repository.save(settings);
    }
}

