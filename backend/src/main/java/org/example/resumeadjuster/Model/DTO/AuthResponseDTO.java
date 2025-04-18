package org.example.resumeadjuster.Model.DTO;

import lombok.Data;


@Data

public class AuthResponseDTO {
    private String token;
    private UserResponseDTO user;
}
