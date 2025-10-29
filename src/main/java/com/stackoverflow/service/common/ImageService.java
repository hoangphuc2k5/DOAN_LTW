package com.stackoverflow.service.common;

import com.stackoverflow.entity.ImageAttachment;
import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.Answer;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.ImageAttachmentRepository;
import com.stackoverflow.repository.QuestionRepository;
import com.stackoverflow.repository.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private ImageAttachmentRepository imageAttachmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    public ImageAttachment saveImage(MultipartFile file, Long questionId, Long answerId, User uploadedBy) throws IOException {
        validateImage(file);
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename with date prefix for better organization
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String newFilename = datePrefix + "-" + UUID.randomUUID().toString() + extension;

        // Save file to disk with error handling
        Path filePath = uploadDir.resolve(newFilename);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to save image: " + e.getMessage());
        }

        // Create and save image attachment record
        ImageAttachment attachment = new ImageAttachment();
        attachment.setFileName(originalFilename);
        attachment.setPath(newFilename);
        attachment.setContentType(file.getContentType());
        attachment.setUploadedBy(uploadedBy);

        // Associate with question or answer if provided
        if (questionId != null) {
            Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
            attachment.setQuestion(question);
        }
        
        if (answerId != null) {
            Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
            attachment.setAnswer(answer);
        }

        return imageAttachmentRepository.save(attachment);
    }

    public ImageAttachment saveQuestionImage(MultipartFile file, Question question) throws IOException {
        validateImage(file);
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file to disk
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create and save image attachment
        ImageAttachment image = new ImageAttachment();
        image.setFileName(originalFilename);
        image.setPath(filename);
        image.setContentType(file.getContentType());
        image.setQuestion(question);
        image.setUploadedBy(question.getAuthor());
        image.setCreatedAt(LocalDateTime.now());
        
        return imageAttachmentRepository.save(image);
    }
    
    public ImageAttachment saveAnswerImage(MultipartFile file, Answer answer) throws IOException {
        validateImage(file);
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file to disk
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create and save image attachment
        ImageAttachment image = new ImageAttachment();
        image.setFileName(originalFilename);
        image.setPath(filename);
        image.setContentType(file.getContentType());
        image.setAnswer(answer);
        image.setUploadedBy(answer.getAuthor());
        image.setCreatedAt(LocalDateTime.now());
        
        return imageAttachmentRepository.save(image);
    }
    
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // 5MB max size
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }
    }

    public Optional<ImageAttachment> findById(Long id) {
        return imageAttachmentRepository.findById(id);
    }

    public void deleteImage(ImageAttachment attachment) throws IOException {
        // Verify if user has permission to delete
        if (!attachment.getUploadedBy().equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            throw new AccessDeniedException("You don't have permission to delete this image");
        }

        // Delete file from disk with error handling
        Path filePath = Paths.get(uploadPath, attachment.getPath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IOException("Failed to delete image file: " + e.getMessage());
        }

        // Remove database record
        imageAttachmentRepository.delete(attachment);
    }

    public List<ImageAttachment> getImagesForQuestion(Question question) {
        return imageAttachmentRepository.findByQuestion(question);
    }

    public List<ImageAttachment> getImagesForAnswer(Answer answer) {
        return imageAttachmentRepository.findByAnswer(answer);
    }

    public byte[] getImageData(ImageAttachment attachment) throws IOException {
        Path filePath = Paths.get(uploadPath, attachment.getPath());
        if (!Files.exists(filePath)) {
            throw new IOException("Image file not found: " + attachment.getPath());
        }
        return Files.readAllBytes(filePath);
    }
}