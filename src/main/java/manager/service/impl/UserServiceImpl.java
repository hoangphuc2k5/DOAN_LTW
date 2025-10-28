package manager.service.impl;

import manager.entity.User;
import manager.repository.UserRepository;
import manager.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public long countActiveMembers() {
        return repo.countActiveUsers();
    }

    @Override
    public List<User> findActiveMembers() {
        return repo.findActiveUsers();
    }

    @Override
    public User findByUsername(String username) {
        return repo.findByUsername(username);
    }
}
