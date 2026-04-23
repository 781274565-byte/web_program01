package com.campusfasttransfer.service;

import com.campusfasttransfer.dto.RegisterForm;
import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> Objects.equals(password, user.getPassword()));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public User register(RegisterForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new RegistrationConflictException(
                    "DUPLICATE_USERNAME",
                    "Username already exists"
            );
        }
        if (userRepository.existsByIdentityNo(form.getIdentityNo())) {
            throw new RegistrationConflictException(
                    "DUPLICATE_IDENTITY_NO",
                    "Identity number already exists"
            );
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setIdentityNo(form.getIdentityNo());
        user.setRole(DEFAULT_ROLE);
        user.setCreatedAt(LocalDateTime.now());

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new RegistrationConflictException(
                    "DUPLICATE_USER",
                    "User registration conflicts with an existing record"
            );
        }
    }
}
