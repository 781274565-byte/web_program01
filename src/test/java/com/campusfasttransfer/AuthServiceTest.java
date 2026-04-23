package com.campusfasttransfer;

import com.campusfasttransfer.entity.User;
import com.campusfasttransfer.repository.UserRepository;
import com.campusfasttransfer.dto.RegisterForm;
import com.campusfasttransfer.service.AuthService;
import com.campusfasttransfer.service.RegistrationConflictException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateReturnsMatchedUserWhenCredentialsAreValid() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("123456");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> authenticated = authService.authenticate("alice", "123456");

        assertTrue(authenticated.isPresent());
        assertSame(user, authenticated.get());
    }

    @Test
    void authenticateReturnsEmptyWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("abcdef");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> authenticated = authService.authenticate("alice", "123456");

        assertTrue(authenticated.isEmpty());
    }

    @Test
    void authenticateReturnsEmptyWhenUserIsMissing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        Optional<User> authenticated = authService.authenticate("alice", "123456");

        assertTrue(authenticated.isEmpty());
    }

    @Test
    void registerSetsUserRoleAndCreatedAt() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByIdentityNo("ID-123")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterForm form = new RegisterForm();
        form.setUsername("alice");
        form.setPassword("123456");
        form.setIdentityNo("ID-123");

        User registered = authService.register(form);

        assertEquals("USER", registered.getRole());
        assertNotNull(registered.getCreatedAt());
    }

    @Test
    void registerThrowsControlledErrorWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        RegisterForm form = new RegisterForm();
        form.setUsername("alice");
        form.setPassword("123456");
        form.setIdentityNo("ID-123");

        RegistrationConflictException exception = assertThrows(RegistrationConflictException.class, () -> authService.register(form));

        assertEquals("DUPLICATE_USERNAME", exception.getCode());
    }

    @Test
    void registerThrowsControlledErrorWhenIdentityNoAlreadyExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByIdentityNo("ID-123")).thenReturn(true);

        RegisterForm form = new RegisterForm();
        form.setUsername("alice");
        form.setPassword("123456");
        form.setIdentityNo("ID-123");

        RegistrationConflictException exception = assertThrows(RegistrationConflictException.class, () -> authService.register(form));

        assertEquals("DUPLICATE_IDENTITY_NO", exception.getCode());
    }
}
