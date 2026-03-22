package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_quarterly_snapshot")
public class BizQuarterlySnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private Long teamId;
    private String teamName;
    private String quarter;
    private String level;
    private String estimatedLevel;
    private BigDecimal totalDgmv;
    private LocalDateTime createdAt;
}
