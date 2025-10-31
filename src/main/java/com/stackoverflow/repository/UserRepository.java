package com.stackoverflow.repository;

import com.stackoverflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u ORDER BY u.reputation DESC")
    Page<User> findAllByOrderByReputationDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Admin features
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByUsernameContainingOrEmailContaining(@Param("keyword") String username, @Param("keyword") String email, Pageable pageable);
    
    List<User> findByRole(String role);
    
    long countByIsActive(Boolean isActive);
    
    long countByIsBanned(Boolean isBanned);
    
    long countByRole(String role);

}

