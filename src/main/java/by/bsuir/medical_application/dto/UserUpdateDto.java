package by.bsuir.medical_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDto {

    @NotBlank(message = "Field can't be blank")
    private String username;

    @NotBlank(message = "Field can't be blank")
    private String password;

    @NotBlank(message = "Field can't be blank")
    @Size(min = 6)
    private String newPassword;

    @NotBlank(message = "Field can't be blank")
    @Size(min = 6)
    private String confirmPassword;
}
