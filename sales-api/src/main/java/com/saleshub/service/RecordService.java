package com.saleshub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saleshub.dto.RecordSubmitRequest;
import com.saleshub.entity.BizDailyRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RecordService {
    void submitRecords(Long userId, RecordSubmitRequest request);
    void submitRecordsNoDateCheck(Long userId, RecordSubmitRequest request);
    boolean hasSubmitted(Long userId, LocalDate date);
    IPage<BizDailyRecord> listRecords(Long userId, String keyword, LocalDate startDate, LocalDate endDate, String sortField, String sortOrder, int page, int size);
    BigDecimal sumDgmv(Long userId, String keyword, LocalDate startDate, LocalDate endDate);
    void deleteRecord(Long id);
    void updateRecord(Long id, java.math.BigDecimal gmv, java.math.BigDecimal refund, String accountNote);
    java.util.List<java.util.Map<String, Object>> getUnfilledUsers(LocalDate date);
}
