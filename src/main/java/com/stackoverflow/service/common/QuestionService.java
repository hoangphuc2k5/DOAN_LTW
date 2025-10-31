package com.stackoverflow.service.common;

import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.Tag;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.QuestionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TagService tagService;

    @PersistenceContext
    private EntityManager entityManager;

    public Question createQuestion(Question question, Set<String> tagNames) {
        Set<Tag> tags = tagService.getOrCreateTags(tagNames);
        question.setTags(tags);
        question.setViews(0);
        question.setVotes(0);
        
        // Moderation logic:
        // - ADMIN/MANAGER: always auto-approve (isApproved = true)
        // - USER: needs moderation (isApproved = false)
        if (isAdminOrManager()) {
            question.setIsApproved(true);
        } else {
            question.setIsApproved(false); // USER questions need admin approval
        }
        
        Question savedQuestion = questionRepository.save(question);
        
        // Increment question count for each tag
        tags.forEach(Tag::incrementQuestionCount);
        
        return savedQuestion;
    }

    public Optional<Question> findById(Long id) {
        return questionRepository.findByIdWithTagsAndAuthor(id);
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findByIdWithTagsAndAuthor(id)
            .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
    }

    public Page<Question> getAllQuestions(Pageable pageable) {
        // Use findAll() instead of findAllByOrderByCreatedAtDesc to avoid duplicate ORDER BY
        // when Pageable already contains Sort
        return questionRepository.findAll(pageable);
    }

    public Page<Question> getQuestionsByVotes(Pageable pageable) {
        // Use custom query to avoid duplicate ORDER BY when Pageable contains Sort
        return questionRepository.findAllByOrderByVotesDesc(pageable);
    }

    public Page<Question> getQuestionsByAuthor(User author, Pageable pageable) {
        return questionRepository.findByAuthor(author, pageable);
    }

    public Page<Question> getQuestionsByTag(Tag tag, Pageable pageable) {
        return questionRepository.findByTag(tag, pageable);
    }

    public Page<Question> searchQuestions(String search, Pageable pageable) {
        return questionRepository.searchQuestions(search, pageable);
    }

    public Question updateQuestion(Question question, Set<String> tagNames) {
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(tagNames);
            question.setTags(tags);
        }
        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        // STEP 1: Get question and decrement tag counts FIRST
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found"));
        
        question.getTags().forEach(tag -> {
            tag.setQuestionCount(Math.max(0, tag.getQuestionCount() - 1));
        });
        
        // STEP 2: Delete from join table using direct EntityManager query
        // This ensures it executes BEFORE deleteById
        int deletedRows = entityManager.createNativeQuery(
                "DELETE FROM question_tags WHERE question_id = :questionId")
                .setParameter("questionId", id)
                .executeUpdate();
        
        System.out.println("Deleted " + deletedRows + " rows from question_tags for question ID: " + id);
        
        // STEP 3: Now safe to delete the question
        // Cascade will handle answers, comments, images
        questionRepository.deleteById(id);
        
        System.out.println("Successfully deleted question ID: " + id);
    }

    public void incrementViews(Question question) {
        question.incrementViews();
        questionRepository.save(question);
    }

    public void upvoteQuestion(Question question, User user) {
        if (!user.getVotedQuestions().contains(question)) {
            question.upvote();
            user.getVotedQuestions().add(question);
            questionRepository.save(question);
        }
    }

    public void downvoteQuestion(Question question, User user) {
        if (user.getVotedQuestions().contains(question)) {
            question.downvote();
            user.getVotedQuestions().remove(question);
            questionRepository.save(question);
        }
    }

    public Long countByAuthor(User author) {
        return questionRepository.countByAuthor(author);
    }

    // ================== ADMIN FEATURES ==================
    
    public void pinQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsPinned(true);
        questionRepository.save(question);
    }

    public void unpinQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsPinned(false);
        questionRepository.save(question);
    }

    public void lockQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsLocked(true);
        questionRepository.save(question);
    }

    public void unlockQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsLocked(false);
        questionRepository.save(question);
    }

    public void approveQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsApproved(true);
        questionRepository.save(question);
    }

    public void rejectQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setIsApproved(false);
        questionRepository.save(question);
    }

    @Transactional
    public Question save(Question question) {
        // Moderation logic for NEW questions only (id == null):
        // - ADMIN/MANAGER: always auto-approve (isApproved = true)
        // - USER: needs moderation (isApproved = false)
        if (question.getId() == null) {
            // This is a new question
            if (isAdminOrManager()) {
                question.setIsApproved(true);
            } else {
                question.setIsApproved(false); // USER questions need admin approval
            }
        }
        // For existing questions, don't change isApproved (let admin control it)
        return questionRepository.save(question);
    }
    
    /**
     * Check if current user is ADMIN or MANAGER
     */
    private boolean isAdminOrManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER"));
    }
}