package org.example.resumeadjuster.Model.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {

    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
}
