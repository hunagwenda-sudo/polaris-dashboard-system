package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_team")
public class SysTeam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long leaderId;
    private BigDecimal targetDgmv;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
