package manager.repository;

import manager.entity.ImageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageAttachmentRepository extends JpaRepository<ImageAttachment, Long> {
    List<ImageAttachment> findByQuestionId(Long questionId);
    List<ImageAttachment> findByAnswerId(Long answerId);
}
