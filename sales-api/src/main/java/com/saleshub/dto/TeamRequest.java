package com.saleshub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TeamRequest {
    @NotBlank(message = "团队名称不能为空")
    private String name;
    private Long leaderId;
    private BigDecimal targetDgmv;
}
