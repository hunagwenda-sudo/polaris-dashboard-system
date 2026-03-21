package com.saleshub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String name;
    private String role;
    private String level;
    private Long teamId;
    private Long groupId;
    private String avatar;
    private Boolean passwordChanged;
}
