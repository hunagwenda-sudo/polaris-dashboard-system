package com.saleshub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GroupRequest {
    @NotBlank(message = "小组名称不能为空")
    private String name;
    private Long leaderId;
    private BigDecimal targetDgmv;
}
