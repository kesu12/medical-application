package by.bsuir.medical_application.dto;

import by.bsuir.medical_application.model.UserRole;
import jakarta.validation.constraints.AssertTrue;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminUpdateDto {

    private Long departmentId;

    private boolean isConfirmed;

    @NotNull(message = "User role is required!")
    private UserRole role;

    private String notes;
    
    // Profile fields
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    @AssertTrue(message = "Department is required for confirmed users")
    public boolean isDepartmentValid() {
        if(isConfirmed){
            return departmentId != null;
        }
        return true;
    }


}
