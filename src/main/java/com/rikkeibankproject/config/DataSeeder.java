package com.rikkeibankproject.config;

import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Đảm bảo cập nhật lại mật khẩu chính xác cho các tài khoản mẫu
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUsername().equals("admin") || user.getUsername().equals("customer")) {
                // Mã hóa lại mật khẩu "123456" chuẩn bằng BCrypt của chính ứng dụng
                user.setPassword(passwordEncoder.encode("123456"));
                userRepository.save(user);
            }
        }
    }
}
