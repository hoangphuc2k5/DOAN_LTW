package manager.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import manager.entity.ImageAttachment;

public interface ImageService {
    void uploadForQuestion(MultipartFile file, Long questionId, Long uploaderId);
    void uploadForAnswer(MultipartFile file, Long answerId, Long uploaderId);
    List<ImageAttachment> getImagesByQuestion(Long questionId);
    List<ImageAttachment> getImagesByAnswer(Long answerId);
}
