package manager.service;

import manager.entity.User;
import java.util.List;

public interface UserService {
    long countActiveMembers();
    List<User> findActiveMembers();
    User findByUsername(String username);
}
