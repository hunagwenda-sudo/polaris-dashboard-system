package com.saleshub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TeamRequest {
    @NotBlank(message = "团队名称不能为空")
    private String name;
    private Long leaderId;
    private List<Long> leaderIds;
    private BigDecimal targetDgmv;
}
