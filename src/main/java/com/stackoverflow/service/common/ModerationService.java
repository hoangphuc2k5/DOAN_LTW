package com.stackoverflow.service.common;

import com.stackoverflow.entity.Comment;
import com.stackoverflow.entity.Question;
import com.stackoverflow.repository.CommentRepository;
import com.stackoverflow.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moderation Service - Kiểm duyệt nội dung
 */
@Service
@Transactional
public class ModerationService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CommentRepository commentRepository;

    // ================== QUESTIONS ==================

    public Page<Question> getPendingQuestions(Pageable pageable) {
        return questionRepository.findByIsApproved(false, pageable);
    }

    public Page<Question> getApprovedQuestions(Pageable pageable) {
        return questionRepository.findByIsApproved(true, pageable);
    }

    public long countPendingQuestions() {
        return questionRepository.countByIsApproved(false);
    }

    public long countApprovedQuestions() {
        return questionRepository.countByIsApproved(true);
    }

    // ================== COMMENTS ==================

    public Page<Comment> getPendingComments(Pageable pageable) {
        return commentRepository.findByIsApproved(false, pageable);
    }

    public Page<Comment> getApprovedComments(Pageable pageable) {
        return commentRepository.findByIsApproved(true, pageable);
    }

    public Page<Comment> getDeletedComments(Pageable pageable) {
        return commentRepository.findByIsDeleted(true, pageable);
    }

    public long countPendingComments() {
        return commentRepository.countByIsApproved(false);
    }

    public long countApprovedComments() {
        return commentRepository.countByIsApproved(true);
    }
}

