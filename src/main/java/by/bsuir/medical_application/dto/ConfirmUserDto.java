package by.bsuir.medical_application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmUserDto {
    
    @NotNull(message = "Department ID is required for confirmation")
    private Long departmentId;
}


