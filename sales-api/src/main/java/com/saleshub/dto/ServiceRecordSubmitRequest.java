package com.saleshub.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ServiceRecordSubmitRequest {
    private LocalDate recordDate;
    private List<Item> items;

    @Data
    public static class Item {
        private String platform;
        private Long shopId;
        private String shopNote;
        private String shift; // morning / evening
        private Integer receptionCount;
        private BigDecimal replyRate;
        private BigDecimal praiseRate;
    }
}
