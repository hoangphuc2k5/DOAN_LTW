package manager.service;

import manager.entity.Question;
import java.util.List;

public interface QuestionService {

    List<Question> findAllApproved();
    List<Question> findAllPending();
    Question findById(Long id);
    void approve(Long id);
    void reject(Long id);
    long countPending();
}
