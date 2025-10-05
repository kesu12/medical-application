package by.bsuir.medical_application.service;

import by.bsuir.medical_application.dto.AuthRequest;
import by.bsuir.medical_application.dto.AuthResponse;
import by.bsuir.medical_application.dto.ChangePasswordRequest;
import by.bsuir.medical_application.dto.UserCreateDto;
import by.bsuir.medical_application.dto.UserResponseDto;
import by.bsuir.medical_application.exceptions.AccountCreatingException;
import by.bsuir.medical_application.model.RefreshToken;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import by.bsuir.medical_application.repository.UserRepository;
import by.bsuir.medical_application.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(UserCreateDto userCreateDto) {
        try {
            if (!userCreateDto.getPassword().equals(userCreateDto.getConfirmPassword())) {
                throw new AccountCreatingException("Passwords do not match");
            }

            if (userRepository.existsByUsernameOrEmail(userCreateDto.getUsername(), userCreateDto.getEmail())) {
                throw new AccountCreatingException("User with this username or email already exists");
            }

            User user = User.builder()
                    .username(userCreateDto.getUsername())
                    .password(passwordEncoder.encode(userCreateDto.getPassword()))
                    .email(userCreateDto.getEmail())
                    .firstName(userCreateDto.getFirstName())
                    .lastName(userCreateDto.getLastName())
                    .middleName(userCreateDto.getMiddleName())
                    .role(UserRole.DEFAULT)
                    .confirmed(true) 
                    .build();

            user = userRepository.save(user);
            
            return generateAuthResponse(user);
            
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage());
            throw new AccountCreatingException("Failed to create user account: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            User user = userRepository.findByUsername(authRequest.getUsername());
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username: " + authRequest.getUsername());
            }

            refreshTokenService.deleteByUserId(user.getUserId());

            return generateAuthResponse(user);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            RefreshToken token = refreshTokenService.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            token = refreshTokenService.verifyExpiration(token);
            User user = token.getUser();

            refreshTokenService.deleteByUserId(user.getUserId());
            return generateAuthResponse(user);

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    public void logout(String refreshToken) {
        try {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getUserId()));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        try {
            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                throw new RuntimeException("New passwords do not match");
            }

            if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
                throw new RuntimeException("New password must be different from current password");
            }

            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);

            refreshTokenService.deleteByUserId(user.getUserId());

        } catch (Exception e) {
            log.error("Error during password change: {}", e.getMessage());
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateTokenWithUserDetails(user);
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
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
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) 
                .user(userResponseDto)
                .build();
    }
}
