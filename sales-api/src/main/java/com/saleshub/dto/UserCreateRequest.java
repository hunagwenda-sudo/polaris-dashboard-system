package com.saleshub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private String phone;
    @NotBlank(message = "角色不能为空")
    private String role;
    private Long teamId;
    private String level;
    private LocalDate birthday;
    private LocalDate hireDate;
}
