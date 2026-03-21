package com.saleshub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saleshub.dto.ServiceRecordSubmitRequest;
import com.saleshub.entity.BizServiceRecord;

import java.time.LocalDate;

public interface ServiceRecordService {
    void submitRecords(Long userId, ServiceRecordSubmitRequest request);
    IPage<BizServiceRecord> listRecords(Long userId, LocalDate startDate, LocalDate endDate, int page, int size);
    IPage<BizServiceRecord> listAllRecords(String keyword, LocalDate startDate, LocalDate endDate, String sortField, String sortOrder, int page, int size);
    java.util.List<java.util.Map<String, Object>> getUnfilledUsers(LocalDate date);
}
