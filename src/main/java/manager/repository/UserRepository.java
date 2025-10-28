package manager.repository;

import manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    // 🔹 Đếm người dùng đang hoạt động
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.isBanned = false")
    long countActiveUsers();

    // 🔹 Lấy danh sách user đang hoạt động
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isBanned = false")
    List<User> findActiveUsers();
}
