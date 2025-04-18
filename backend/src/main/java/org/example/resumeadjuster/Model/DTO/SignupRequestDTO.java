package org.example.resumeadjuster.Model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SignupRequestDTO {
    @NotBlank(message = "First name is required")
    @Size(min=1,max=100,message="First name must be between 1 to 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min=1,max=100,message="Last name must be between 1 to 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message="Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min=6,message="Email must be valid")
    private String password;

}
