package manager.service.impl;

import manager.entity.Question;
import manager.repository.QuestionRepository;
import manager.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository repo;

    public QuestionServiceImpl(QuestionRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Question> findAllApproved() {
        return repo.findAllApproved();
    }

    @Override
    public List<Question> findAllPending() {
        return repo.findAllPending();
    }

    @Override
    public Question findById(Long id) {
        Question q = repo.findDetailById(id);
        if (q == null) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi có ID: " + id);
        }
        return q;
    }

    @Override
    public void approve(Long id) {
        Question q = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
        q.setIsApproved(true);
        repo.save(q);
    }

    @Override
    public void reject(Long id) {
        Question q = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
        q.setIsApproved(false);
        repo.save(q);
    }

    @Override
    public long countPending() {
        return repo.countPending();
    }
}
