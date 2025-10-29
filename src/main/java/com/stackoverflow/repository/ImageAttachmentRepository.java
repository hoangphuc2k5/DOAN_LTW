package com.stackoverflow.repository;

import com.stackoverflow.entity.ImageAttachment;
import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageAttachmentRepository extends JpaRepository<ImageAttachment, Long> {
    List<ImageAttachment> findByQuestion(Question question);
    List<ImageAttachment> findByAnswer(Answer answer);
}