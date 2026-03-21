package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String code;
    private String label;
    private String iconUrl;
    private Integer sort;
    private String status;

    @TableLogic
    private Integer deleted;
}
