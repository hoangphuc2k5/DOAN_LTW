package com.edumoet.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.edumoet.entity.Attachment;
import com.edumoet.repository.AttachmentRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;
    private final AttachmentRepository attachmentRepository;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir,
                              AttachmentRepository attachmentRepository) throws IOException {
        this.attachmentRepository = attachmentRepository;
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.storageLocation);
    }

    public Attachment store(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > -1) ext = original.substring(idx);
        
        // Generate unique filename: att_{timestamp}_{uuid}.{ext}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String stored = String.format("att_%s_%s%s", timestamp, uuid, ext);
        
        // Check if file exists, append counter
        Path target = this.storageLocation.resolve(stored);
        int counter = 1;
        while (Files.exists(target)) {
            stored = String.format("att_%s_%s_%d%s", timestamp, uuid, counter, ext);
            target = this.storageLocation.resolve(stored);
            counter++;
        }
        
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Attachment a = new Attachment();
        a.setFilename(stored);
        a.setOriginalName(original);
        a.setContentType(file.getContentType());
        a.setSize(file.getSize());
        a.setUploadedAt(LocalDateTime.now());
        attachmentRepository.save(a);
        return a;
    }

    public Resource loadAsResource(String filename) throws MalformedURLException {
        Path file = storageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) return resource;
        throw new MalformedURLException("File not found " + filename);
    }

    public Path getStorageLocation() {
        return storageLocation;
    }
}