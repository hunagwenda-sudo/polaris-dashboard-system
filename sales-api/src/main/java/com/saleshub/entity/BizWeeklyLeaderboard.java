package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_weekly_leaderboard")
public class BizWeeklyLeaderboard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String weekLabel;
    private Integer rankNum;
    private Long userId;
    private String userName;
    private String userRole;
    private String userLevel;
    private String estimatedLevel;
    private BigDecimal dgmv;
    private LocalDateTime createdAt;
}
