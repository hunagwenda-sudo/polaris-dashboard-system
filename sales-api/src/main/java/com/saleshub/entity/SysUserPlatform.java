package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_platform")
public class SysUserPlatform {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String platformCode;
    private Long accountId;

    @TableLogic
    private Integer deleted;
}
