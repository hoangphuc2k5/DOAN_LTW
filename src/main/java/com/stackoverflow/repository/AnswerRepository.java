package com.stackoverflow.repository;

import com.stackoverflow.entity.Answer;
import com.stackoverflow.entity.Question;
import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    @Query("SELECT a FROM Answer a LEFT JOIN FETCH a.images WHERE a.question = :question ORDER BY a.votes DESC, a.createdAt DESC")
    List<Answer> findByQuestionOrderByVotesDescCreatedAtDesc(@Param("question") Question question);
    
    Page<Answer> findByAuthor(User author, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.author = :author")
    Long countByAuthor(@Param("author") User author);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question = :question")
    Long countByQuestion(@Param("question") Question question);
}

