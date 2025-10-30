package com.edumoet.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.edumoet.entity.Answer;
import com.edumoet.entity.ImageAttachment;
import com.edumoet.entity.Question;
import com.edumoet.entity.User;
import com.edumoet.repository.AnswerRepository;
import com.edumoet.repository.ImageAttachmentRepository;
import com.edumoet.repository.QuestionRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

        // Generate unique filename: type{id}_{timestamp}_{uuid}.{ext}
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // Determine type prefix
        String typePrefix = questionId != null ? "q" + questionId : 
                           answerId != null ? "a" + answerId : "img";
        String newFilename = String.format("%s_%s_%s%s", typePrefix, timestamp, uuid, extension);
        
        // Check if file exists (extremely rare), append counter
        Path filePath = uploadDir.resolve(newFilename);
        int counter = 1;
        while (Files.exists(filePath)) {
            newFilename = String.format("%s_%s_%s_%d%s", typePrefix, timestamp, uuid, counter, extension);
            filePath = uploadDir.resolve(newFilename);
            counter++;
        }

        // Save file to disk with error handling
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

        // Generate unique filename: q{questionId}_{timestamp}_{uuid}.{ext}
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("q%d_%s_%s%s", question.getId(), timestamp, uuid, extension);
        
        // Check if file exists, append counter
        Path filePath = uploadDir.resolve(filename);
        int counter = 1;
        while (Files.exists(filePath)) {
            filename = String.format("q%d_%s_%s_%d%s", question.getId(), timestamp, uuid, counter, extension);
            filePath = uploadDir.resolve(filename);
            counter++;
        }
        
        // Save file to disk
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

        // Generate unique filename: a{answerId}_{timestamp}_{uuid}.{ext}
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("a%d_%s_%s%s", answer.getId(), timestamp, uuid, extension);
        
        // Check if file exists, append counter
        Path filePath = uploadDir.resolve(filename);
        int counter = 1;
        while (Files.exists(filePath)) {
            filename = String.format("a%d_%s_%s_%d%s", answer.getId(), timestamp, uuid, counter, extension);
            filePath = uploadDir.resolve(filename);
            counter++;
        }
        
        // Save file to disk
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
        System.out.println("=== ImageService.deleteImage() ===");
        System.out.println("Image ID: " + attachment.getId());
        System.out.println("Image Path: " + attachment.getPath());
        System.out.println("Uploaded By: " + (attachment.getUploadedBy() != null ? attachment.getUploadedBy().getUsername() : "NULL"));
        
        // Skip permission check - let controller handle it
        // The controller already verified the user is the question author
        
        // Delete file from disk with error handling
        Path filePath = Paths.get(uploadPath, attachment.getPath());
        System.out.println("Full file path: " + filePath.toAbsolutePath());
        System.out.println("File exists: " + Files.exists(filePath));
        
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            System.out.println("File deleted: " + deleted);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to delete physical file: " + e.getMessage());
            // Don't throw - continue to delete from DB
        }

        // Remove database record
        imageAttachmentRepository.delete(attachment);
        System.out.println("✅ Image record deleted from database");
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
    
    // ================== MANAGER FEATURES ==================
    
    /**
     * Get images by question ID
     */
    public List<ImageAttachment> getImagesByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return imageAttachmentRepository.findByQuestion(question);
    }
    
    /**
     * Upload image for question - manager version
     */
    public ImageAttachment uploadForQuestion(MultipartFile file, Long questionId, Long uploaderId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            return saveQuestionImage(file, question);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Upload image for answer - manager version
     */
    public ImageAttachment uploadForAnswer(MultipartFile file, Long answerId, Long uploaderId) {
        try {
            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new RuntimeException("Answer not found"));
            return saveAnswerImage(file, answer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    
    // ================== AVATAR FEATURES ==================
    
    /**
     * Lưu avatar từ base64 string (cho cropped image)
     */
    public String saveAvatarFromBase64(String base64Data, String prefix) {
        try {
            // Remove data:image/...;base64, prefix if exists
            String imageData = base64Data;
            if (base64Data.contains(",")) {
                imageData = base64Data.split(",")[1];
            }
            
            // Decode base64
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            // Create avatars directory if it doesn't exist
            Path avatarsDir = Paths.get(uploadPath, "avatars");
            if (!Files.exists(avatarsDir)) {
                Files.createDirectories(avatarsDir);
            }
            
            // Generate unique filename: prefix_{timestamp}_{uuid}.jpg
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String filename = String.format("%s_%s_%s.jpg", prefix, timestamp, uuid);
            
            // Check if file exists, append counter
            Path filePath = avatarsDir.resolve(filename);
            int counter = 1;
            while (Files.exists(filePath)) {
                filename = String.format("%s_%s_%s_%d.jpg", prefix, timestamp, uuid, counter);
                filePath = avatarsDir.resolve(filename);
                counter++;
            }
            
            // Write file to disk
            Files.write(filePath, imageBytes);
            
            return filename;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save avatar: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xóa avatar
     */
    public void deleteAvatar(String filename) {
        try {
            Path filePath = Paths.get(uploadPath, "avatars", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to delete avatar file: " + e.getMessage());
            // Don't throw - not critical
        }
    }
}