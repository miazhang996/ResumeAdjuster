package org.example.resumeadjuster.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;



@Data
public class LoginRequestDTO {
//    @NotBlank(message="Email is required")
    private String email;

//    @NotBlank(message="Password is requried")
    private String password;


}
