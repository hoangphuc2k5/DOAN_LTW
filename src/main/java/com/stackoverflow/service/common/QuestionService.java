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
        // Return ALL questions (for admin/manager - includes both approved and pending)
        return questionRepository.findAll(pageable);
    }
    
    public Page<Question> getAllApprovedQuestions(Pageable pageable) {
        // Only return approved questions
        return questionRepository.findByIsApproved(true, pageable);
    }
    
    public Page<Question> getAllPendingQuestions(Pageable pageable) {
        // Only return pending questions
        return questionRepository.findByIsApproved(false, pageable);
    }

    public Page<Question> getQuestionsByVotes(Pageable pageable) {
        // Only return approved questions sorted by votes
        // Note: findAllByOrderByVotesDesc already filters by isApproved = true
        return questionRepository.findAllByOrderByVotesDesc(pageable);
    }
    
    /**
     * Get questions for public view (approved and not locked)
     * Only Admin and Manager can see locked questions
     */
    public Page<Question> getPublicQuestions(Pageable pageable) {
        return questionRepository.findByIsApprovedAndIsLocked(true, false, pageable);
    }
    
    /**
     * Get questions by votes for public view (approved and not locked)
     */
    public Page<Question> getPublicQuestionsByVotes(Pageable pageable) {
        return questionRepository.findByIsApprovedAndIsLockedOrderByVotesDesc(true, false, pageable);
    }

    public Page<Question> getQuestionsByAuthor(User author, Pageable pageable) {
        // Return all questions for the author (they can see their own pending questions)
        return questionRepository.findByAuthor(author, pageable);
    }
    
    public Page<Question> getApprovedQuestionsByAuthor(User author, Pageable pageable) {
        // Return only approved questions for the author (for public viewing)
        return questionRepository.findByAuthorAndIsApproved(author, true, pageable);
    }
    
    public Page<Question> getPendingQuestionsByAuthor(User author, Pageable pageable) {
        // Return only pending questions for the author
        return questionRepository.findByAuthorAndIsApproved(author, false, pageable);
    }

    public Page<Question> getQuestionsByTag(Tag tag, Pageable pageable) {
        // Only return approved questions for tag listing
        // Note: findByTag already filters by isApproved = true
        return questionRepository.findByTag(tag, pageable);
    }

    public Page<Question> searchQuestions(String search, Pageable pageable) {
        // Only return approved questions in search results
        // Note: searchQuestions already filters by isApproved = true
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
        // If already upvoted, do nothing (can't upvote twice)
        if (user.getVotedQuestions().contains(question)) {
            return;
        }
        
        // If previously downvoted, remove downvote first (undo downvote)
        // This is tracked by: user NOT in votedQuestions but vote count is low
        // For simplicity, always allow upvote
        
        question.upvote();
        user.getVotedQuestions().add(question);
        questionRepository.save(question);
    }

    public void downvoteQuestion(Question question, User user) {
        // If user has upvoted, remove upvote and downvote (net change: -2)
        if (user.getVotedQuestions().contains(question)) {
            question.downvote(); // Remove upvote
            question.downvote(); // Apply downvote
            user.getVotedQuestions().remove(question);
            questionRepository.save(question);
        } else {
            // User hasn't voted yet, apply downvote directly
            question.downvote();
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
    
    // ================== MANAGER FEATURES ==================
    
    /**
     * Find all approved questions
     */
    public java.util.List<Question> findAllApproved() {
        return questionRepository.findByIsApprovedOrderByCreatedAtDesc(true);
    }
    
    /**
     * Find all pending (not approved) questions
     */
    public java.util.List<Question> findAllPending() {
        return questionRepository.findByIsApprovedOrderByCreatedAtDesc(false);
    }
    
    /**
     * Approve question - alias for approveQuestion
     */
    public void approve(Long id) {
        approveQuestion(id);
    }
    
    /**
     * Reject question - alias for rejectQuestion
     */
    public void reject(Long id) {
        rejectQuestion(id);
    }
    
    /**
     * Count pending questions
     */
    public long countPending() {
        return questionRepository.countByIsApproved(false);
    }
    
    public long countAllQuestions() {
        return questionRepository.count();
    }
    
    public long countApproved() {
        return questionRepository.countByIsApproved(true);
    }
}