package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_platform_account")
public class SysPlatformAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String platformCode;
    private String accountName;
    private Integer sort;
    private String status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
