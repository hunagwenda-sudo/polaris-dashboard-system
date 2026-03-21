package com.saleshub.service;

public interface AuditService {
    void log(Long userId, String username, String action, String targetType, Long targetId, String detail);
}
