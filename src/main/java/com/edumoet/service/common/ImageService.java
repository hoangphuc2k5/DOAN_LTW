package com.edumoet.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.edumoet.entity.Answer;
import com.edumoet.entity.ImageAttachment;
import com.edumoet.entity.Question;
import com.edumoet.entity.User;
import com.edumoet.repository.AnswerRepository;
import com.edumoet.repository.ImageAttachmentRepository;
import com.edumoet.repository.QuestionRepository;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.base-folder:uploads}")
    private String baseFolder;

    @Autowired
    private ImageAttachmentRepository imageAttachmentRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private S3Client s3Client;

    // ================== SAVE IMAGE ==================
    public ImageAttachment saveImage(MultipartFile file, Long questionId, Long answerId, User uploadedBy) throws IOException {
        validateImage(file);

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String typePrefix = questionId != null ? "q" + questionId :
                answerId != null ? "a" + answerId : "img";
        String newFilename = String.format("%s_%s_%s%s", typePrefix, timestamp, uuid, extension);
        String key = baseFolder + "/" + newFilename;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (S3Exception e) {
            throw new IOException("Failed to upload image to S3: " + e.awsErrorDetails().errorMessage(), e);
        }

        ImageAttachment attachment = new ImageAttachment();
        attachment.setFileName(originalFilename);
        attachment.setPath(newFilename);
        attachment.setContentType(file.getContentType());
        attachment.setUploadedBy(uploadedBy);

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

    // ================== QUESTION IMAGE ==================
    public ImageAttachment saveQuestionImage(MultipartFile file, Question question) throws IOException {
        validateImage(file);
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains(".")) ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("q%d_%s_%s%s", question.getId(), timestamp, uuid, extension);
        String key = baseFolder + "/" + filename;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        ImageAttachment image = new ImageAttachment();
        image.setFileName(originalFilename);
        image.setPath(filename);
        image.setContentType(file.getContentType());
        image.setQuestion(question);
        image.setUploadedBy(question.getAuthor());
        image.setCreatedAt(LocalDateTime.now());
        return imageAttachmentRepository.save(image);
    }

    // ================== ANSWER IMAGE ==================
    public ImageAttachment saveAnswerImage(MultipartFile file, Answer answer) throws IOException {
        validateImage(file);
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains(".")) ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("a%d_%s_%s%s", answer.getId(), timestamp, uuid, extension);
        String key = baseFolder + "/" + filename;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        ImageAttachment image = new ImageAttachment();
        image.setFileName(originalFilename);
        image.setPath(filename);
        image.setContentType(file.getContentType());
        image.setAnswer(answer);
        image.setUploadedBy(answer.getAuthor());
        image.setCreatedAt(LocalDateTime.now());
        return imageAttachmentRepository.save(image);
    }

    // ================== DELETE IMAGE ==================
    public void deleteImage(ImageAttachment attachment) throws IOException {
        String key = baseFolder + "/" + attachment.getPath();
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.out.println("⚠️ Failed to delete image from S3: " + e.awsErrorDetails().errorMessage());
        }
        imageAttachmentRepository.delete(attachment);
        System.out.println("✅ Image record deleted from DB and S3");
    }

    // ================== GET IMAGE DATA ==================
    public byte[] getImageData(ImageAttachment attachment) throws IOException {
        String key = baseFolder + "/" + attachment.getPath();
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucketName).key(key).build());
            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            throw new IOException("Failed to download image from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    // ================== OTHER METHODS ==================
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("File must be an image");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("File size must be less than 5MB");
    }

    public Optional<ImageAttachment> findById(Long id) {
        return imageAttachmentRepository.findById(id);
    }

    public List<ImageAttachment> getImagesForQuestion(Question question) {
        return imageAttachmentRepository.findByQuestion(question);
    }

    public List<ImageAttachment> getImagesForAnswer(Answer answer) {
        return imageAttachmentRepository.findByAnswer(answer);
    }

    public List<ImageAttachment> getImagesByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return imageAttachmentRepository.findByQuestion(question);
    }

    public ImageAttachment uploadForQuestion(MultipartFile file, Long questionId, Long uploaderId) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            return saveQuestionImage(file, question);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

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
    public String saveAvatarFromBase64(String base64Data, String prefix) {
        try {
            String imageData = base64Data.contains(",") ? base64Data.split(",")[1] : base64Data;
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String filename = String.format("%s_%s_%s.jpg", prefix, timestamp, uuid);
            String key = baseFolder + "/avatars/" + filename;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(imageBytes)
            );
            return filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save avatar to S3: " + e.getMessage(), e);
        }
    }

    public void deleteAvatar(String filename) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(baseFolder + "/avatars/" + filename)
                    .build());
        } catch (S3Exception e) {
            System.out.println("⚠️ Failed to delete avatar from S3: " + e.awsErrorDetails().errorMessage());
        }
    }
}
