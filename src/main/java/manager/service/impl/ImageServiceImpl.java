package manager.service.impl;

import manager.entity.*;
import manager.repository.*;
import manager.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageAttachmentRepository imageRepo;
    private final QuestionRepository questionRepo;
    private final AnswerRepository answerRepo;
    private final UserRepository userRepo;

    @Value("${upload.path:uploads}")
    private String uploadDir;

    public ImageServiceImpl(ImageAttachmentRepository imageRepo, QuestionRepository questionRepo,
                            AnswerRepository answerRepo, UserRepository userRepo) {
        this.imageRepo = imageRepo;
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void uploadForQuestion(MultipartFile file, Long questionId, Long uploaderId) {
        save(file, questionId, null, uploaderId);
    }

    @Override
    public void uploadForAnswer(MultipartFile file, Long answerId, Long uploaderId) {
        save(file, null, answerId, uploaderId);
    }

    private void save(MultipartFile file, Long questionId, Long answerId, Long uploaderId) {
        try {
            User uploader = userRepo.findById(uploaderId).orElseThrow();
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ImageAttachment img = new ImageAttachment();
            img.setFileName(fileName);
            img.setContentType(file.getContentType());
            img.setPath("/uploads/" + fileName);
            img.setUploadedBy(uploader);

            if (questionId != null) img.setQuestion(questionRepo.findById(questionId).orElseThrow());
            if (answerId != null) img.setAnswer(answerRepo.findById(answerId).orElseThrow());

            imageRepo.save(img);
        } catch (IOException e) {
            throw new RuntimeException("Error saving image", e);
        }
    }

    @Override
    public List<ImageAttachment> getImagesByQuestion(Long questionId) {
        return imageRepo.findByQuestionId(questionId);
    }

    @Override
    public List<ImageAttachment> getImagesByAnswer(Long answerId) {
        return imageRepo.findByAnswerId(answerId);
    }
}
