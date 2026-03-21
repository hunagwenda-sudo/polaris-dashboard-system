package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;        // partner / leader / sales
    private String name;        // 合伙人 / 组长 / 销售
    private String description;

    @TableLogic
    private Integer deleted;
}
