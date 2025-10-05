package by.bsuir.medical_application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 25)
    private String username;

    @Size(min = 6)
    @NotBlank(message = "Password is required")
    private String password;

    @Size(min = 6)
    @NotBlank(message = "Password confirm field can't be blank")
    private String confirmPassword;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;

}
