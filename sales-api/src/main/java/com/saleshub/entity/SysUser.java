package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String avatar;
    private String role;       // admin / partner / sales
    private Long teamId;
    private Long groupId;
    private String level;      // K3-K6
    private String estimatedLevel; // 预估职级（用于播报变化检测）
    private java.math.BigDecimal targetDgmv; // 个人季度目标
    private String status;     // active / inactive
    private LocalDate birthday;
    private LocalDate hireDate;
    private Integer remindEnabled; // 1=提醒填报 0=不提醒
    private String requiredPlatforms; // 需要每日填报的平台code，逗号分隔
    private Boolean passwordChanged; // 是否已修改过密码

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
