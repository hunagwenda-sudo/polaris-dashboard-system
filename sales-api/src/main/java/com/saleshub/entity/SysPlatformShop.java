package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_platform_shop")
public class SysPlatformShop {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String platformCode;
    private String shopName;
    private Integer sort;
    private String status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
