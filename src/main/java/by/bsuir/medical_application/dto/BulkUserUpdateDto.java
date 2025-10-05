package by.bsuir.medical_application.dto;

import by.bsuir.medical_application.model.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUserUpdateDto {
    
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<Long> userIds;
    
    @NotNull(message = "Action is required")
    private BulkAction action;
    
    private UserRole role;
    
    private Long departmentId;
    
    public enum BulkAction {
        CONFIRM,
        DEACTIVATE,
        CHANGE_ROLE,
        ASSIGN_DEPARTMENT,
        DELETE
    }
}


