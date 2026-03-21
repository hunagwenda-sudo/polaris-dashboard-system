package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String action;       // CREATE / UPDATE / DELETE
    private String targetType;   // SysUser / SysTeam / BizDailyRecord 等
    private Long targetId;
    private String detail;       // 变更详情 JSON
    private LocalDateTime createdAt;
}
