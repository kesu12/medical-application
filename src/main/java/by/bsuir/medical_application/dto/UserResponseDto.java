package by.bsuir.medical_application.dto;

import by.bsuir.medical_application.model.Department;
import by.bsuir.medical_application.model.User;
import by.bsuir.medical_application.model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    @JsonIgnoreProperties({"users"})
    private Department department;

    @Builder.Default
    private Boolean confirmed = false;
    
    private Instant createdAt;
    private Instant updatedAt;
    private String firstName;
    private String lastName;
    private String middleName;
    private String avatarUrl;
    private String phoneNumber;
    
    @JsonIgnoreProperties({"assignedNurse", "assignedDoctor", "department", "refreshTokens", "password", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
    private User assignedNurse;
    
    @JsonIgnoreProperties({"assignedNurse", "assignedDoctor", "department", "refreshTokens", "password", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
    private User assignedDoctor;
    private String treatment;

    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.middleName = user.getMiddleName();
        this.avatarUrl = user.getAvatarUrl();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.department = user.getDepartment();
        this.confirmed = user.getConfirmed();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.assignedNurse = user.getAssignedNurse();
        this.assignedDoctor = user.getAssignedDoctor();
        this.treatment = user.getTreatment();
    }
}
