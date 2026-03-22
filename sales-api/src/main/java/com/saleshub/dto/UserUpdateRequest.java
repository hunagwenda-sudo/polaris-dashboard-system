package com.saleshub.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String name;
    private String phone;
    private String role;
    private Long teamId;
    private String level;
    private BigDecimal targetDgmv;
    private LocalDate birthday;
    private LocalDate hireDate;
    private Integer remindEnabled;
    private String requiredPlatforms;
}
