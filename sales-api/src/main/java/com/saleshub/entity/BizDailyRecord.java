package com.saleshub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_daily_record")
public class BizDailyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate recordDate;
    private String platform;
    private Long accountId;
    private String accountNote;
    private BigDecimal gmv;
    private BigDecimal refund;
    private BigDecimal dgmv;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    /** 非数据库字段，查询时填充用户姓名 */
    @TableField(exist = false)
    private String userName;
}
