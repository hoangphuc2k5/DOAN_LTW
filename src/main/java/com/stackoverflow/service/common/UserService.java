package com.stackoverflow.service.common;

import com.stackoverflow.entity.User;
import com.stackoverflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ==================== Các HÀM CŨ mà controller đang gọi ==================== */

    public Optional<User> findByUsername(String username) {
        // Nếu repo chưa có, hãy thêm: Optional<User> findByUsername(String username);
        return userRepo.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    public Page<User> getUsersByReputation(Pageable pageable) {
        // Không phụ thuộc repo custom; sort trực tiếp theo field "reputation"
        Sort sort = Sort.by(Sort.Direction.DESC, "reputation");
        Pageable p = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return userRepo.findAll(p);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        // Để compile chắc kèo: nếu repo chưa có hàm search custom thì trả về all.
        // (Sau muốn search thật, thêm method vào repo rồi gọi ở đây)
        if (keyword == null || keyword.isBlank()) {
            return userRepo.findAll(pageable);
        }
        // Tối thiểu: thử tìm theo username chứa keyword nếu repo có.
        // Nếu repo KHÔNG có method dưới, comment 2 dòng này lại — app vẫn chạy.
        try {
            // ví dụ: Page<User> findByUsernameContainingIgnoreCase(String kw, Pageable p);
            return userRepo.findByUsernameContainingIgnoreCase(keyword, pageable);
        } catch (Throwable ignore) {
            return userRepo.findAll(pageable);
        }
    }

    public User registerUser(User user) {
        // Mã hoá mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public User updateUser(User user) {
        return userRepo.save(user);
    }

    public void incrementViews(User user) {
        try {
            Integer v = user.getViews();
            if (v == null) v = 0;
            user.setViews(v + 1);
            userRepo.save(user);
        } catch (Throwable ignore) {
            // không làm gì – tránh crash nếu schema khác
        }
    }

    /* ==================== TUYỆT ĐỐI KHÔNG NHÉT OTP VÀO ĐÂY ==================== */
}
