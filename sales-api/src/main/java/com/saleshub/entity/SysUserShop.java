package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_shop")
public class SysUserShop {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String platformCode;
    private Long shopId;
    @TableLogic
    private Integer deleted;
}
