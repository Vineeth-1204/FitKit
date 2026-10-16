package com.fitkit.service;

import com.fitkit.domain.entity.User;
import com.fitkit.domain.enums.UserRole;
import com.fitkit.dto.LoginRequestDto;
import com.fitkit.dto.RegisterRequestDto;
import com.fitkit.dto.UserProfileDto;
import com.fitkit.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @org.springframework.beans.factory.annotation.Value("${fitkit.auth.seed-demo:false}")
    private boolean seedDemoEnabled;

    @PostConstruct
    public void initDemoAccount() {
        if (!seedDemoEnabled) {
            return;
        }
        userRepository.findByEmail("demo@fitkit.local").ifPresent(user -> {
            if (!passwordEncoder.matches("FitKit2026!", user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode("FitKit2026!"));
                userRepository.save(user);
            }
        });
    }

    @Transactional
    public UserProfileDto register(RegisterRequestDto request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        String name = (request.getFullName() != null && !request.getFullName().trim().isEmpty())
                ? request.getFullName().trim()
                : "FitKit User";

        User user = new User(normalizedEmail, passwordHash, name, UserRole.ROLE_USER);
        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    public UserProfileDto login(LoginRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new BadCredentialsException("Email and password are required.");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        // Session fixation protection: regenerate session ID
        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("User account not found."));

        return mapToDto(user);
    }

    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        String normalizedEmail = principal.getName().trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(this::mapToDto)
                .orElse(null);
    }

    private UserProfileDto mapToDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
