package com.example.consultant_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private Long id;
    private String name;
    private String email;
    private String imageUrl;
    private Boolean hasPassword;
    private Boolean hasGoogleAuth;
}
