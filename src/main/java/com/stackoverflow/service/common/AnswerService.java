package com.stackoverflow.service.common;

import com.stackoverflow.entity.Answer;
import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.AnswerRepository;
import com.stackoverflow.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private QuestionRepository questionRepository;

    public Answer createAnswer(Answer answer) {
        answer.setVotes(0);
        answer.setIsAccepted(false);
        
        Answer savedAnswer = answerRepository.save(answer);
        
        // Increment answer count
        Question question = answer.getQuestion();
        if (question != null) {
            question.setAnswerCount(question.getAnswerCount() + 1);
            questionRepository.save(question);
        }
        
        return savedAnswer;
    }

    public Optional<Answer> findById(Long id) {
        return answerRepository.findById(id);
    }

    public List<Answer> getAnswersByQuestion(Question question) {
        return answerRepository.findByQuestionOrderByVotesDescCreatedAtDesc(question);
    }

    public Page<Answer> getAnswersByAuthor(User author, Pageable pageable) {
        return answerRepository.findByAuthor(author, pageable);
    }

    public Answer updateAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long id) {
        Optional<Answer> answerOpt = answerRepository.findById(id);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            Question question = answer.getQuestion();
            
            // Decrement answer count
            if (question != null && question.getAnswerCount() > 0) {
                question.setAnswerCount(question.getAnswerCount() - 1);
                questionRepository.save(question);
            }
            
            answerRepository.deleteById(id);
        }
    }

    public void acceptAnswer(Answer answer) {
        Question question = answer.getQuestion();
        
        // Unaccept previous answer if exists
        if (question.getAcceptedAnswer() != null) {
            Answer previousAccepted = question.getAcceptedAnswer();
            previousAccepted.setIsAccepted(false);
            answerRepository.save(previousAccepted);
        }
        
        // Accept new answer
        answer.setIsAccepted(true);
        question.setAcceptedAnswer(answer);
        answerRepository.save(answer);
    }

    public void upvoteAnswer(Answer answer, User user) {
        // If already upvoted, do nothing (can't upvote twice)
        if (user.getVotedAnswers().contains(answer)) {
            return;
        }
        
        // If previously downvoted, remove downvote first (undo downvote)
        // For simplicity, always allow upvote
        
        answer.upvote();
        user.getVotedAnswers().add(answer);
        answerRepository.save(answer);
    }

    public void downvoteAnswer(Answer answer, User user) {
        // If user has upvoted, remove upvote and downvote (net change: -2)
        if (user.getVotedAnswers().contains(answer)) {
            answer.downvote(); // Remove upvote
            answer.downvote(); // Apply downvote
            user.getVotedAnswers().remove(answer);
            answerRepository.save(answer);
        } else {
            // User hasn't voted yet, apply downvote directly
            answer.downvote();
            answerRepository.save(answer);
        }
    }

    public Long countByAuthor(User author) {
        return answerRepository.countByAuthor(author);
    }

    public Long countByQuestion(Question question) {
        return answerRepository.countByQuestion(question);
    }
    
    public long countAll() {
        return answerRepository.count();
    }
    
    // ================== ADMIN FEATURES ==================
    
    public Page<Answer> getAllAnswers(Pageable pageable) {
        return answerRepository.findAll(pageable);
    }
    
    public Page<Answer> searchAnswers(String keyword, Pageable pageable) {
        return answerRepository.findByBodyContaining(keyword, pageable);
    }
    
    /**
     * Save answer (for updating existing answer)
     */
    public Answer save(Answer answer) {
        return answerRepository.save(answer);
    }
}

