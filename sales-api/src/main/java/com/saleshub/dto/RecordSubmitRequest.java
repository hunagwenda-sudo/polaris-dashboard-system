package com.saleshub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecordSubmitRequest {
    @NotNull(message = "日期不能为空")
    private LocalDate recordDate;

    /** 管理员补录时指定的目标用户ID */
    private Long userId;

    @NotEmpty(message = "至少提交一条记录")
    private List<RecordItem> items;

    @Data
    public static class RecordItem {
        @NotBlank(message = "平台不能为空")
        private String platform;
        private Long accountId;     // 关联 sys_platform_account.id
        private String accountNote; // 账号备注，可为空
        @NotNull @DecimalMin(value = "0", message = "GMV不能为负")
        private BigDecimal gmv;
        @NotNull @DecimalMin(value = "0", message = "退款不能为负")
        private BigDecimal refund;
    }
}
