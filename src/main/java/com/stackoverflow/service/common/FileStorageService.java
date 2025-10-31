package com.stackoverflow.service.common;

import com.stackoverflow.entity.Attachment;
import com.stackoverflow.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
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
        String stored = UUID.randomUUID().toString() + ext;
        Path target = this.storageLocation.resolve(stored);
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