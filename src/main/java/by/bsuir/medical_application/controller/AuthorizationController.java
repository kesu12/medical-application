package by.bsuir.medical_application.controller;

import by.bsuir.medical_application.dto.AuthRequest;
import by.bsuir.medical_application.dto.AuthResponse;
import by.bsuir.medical_application.dto.ChangePasswordRequest;
import by.bsuir.medical_application.dto.RefreshTokenRequest;
import by.bsuir.medical_application.dto.UserCreateDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserCreateDto userCreateDto) {
        AuthResponse response = authService.register(userCreateDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            AuthResponse response = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            authService.logout(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            UserResponseDto userResponseDto = UserResponseDto.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .middleName(user.getMiddleName())
                    .role(user.getRole())
                    .department(user.getDepartment())
                    .confirmed(user.getConfirmed())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .assignedNurse(user.getAssignedNurse())
                    .assignedDoctor(user.getAssignedDoctor())
                    .build();
            return ResponseEntity.ok(userResponseDto);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                               Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            authService.changePassword(user, changePasswordRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}


