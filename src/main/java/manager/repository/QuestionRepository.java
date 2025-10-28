package manager.repository;

import manager.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 🔹 Lấy danh sách câu hỏi đã duyệt (chỉ fetch author)
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT q FROM Question q WHERE q.isApproved = true ORDER BY q.createdAt DESC")
    List<Question> findAllApproved();

    // 🔹 Lấy danh sách câu hỏi chưa duyệt (chỉ fetch author)
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT q FROM Question q WHERE q.isApproved = false ORDER BY q.createdAt DESC")
    List<Question> findAllPending();

    // 🔹 Đếm câu hỏi chưa duyệt
    @Query("SELECT COUNT(q) FROM Question q WHERE q.isApproved = false")
    long countPending();

    // 🔹 Lấy chi tiết câu hỏi (fetch author + images)
    @EntityGraph(attributePaths = {"author", "images"})
    @Query("SELECT q FROM Question q WHERE q.id = :id")
    Question findDetailById(Long id);
}
