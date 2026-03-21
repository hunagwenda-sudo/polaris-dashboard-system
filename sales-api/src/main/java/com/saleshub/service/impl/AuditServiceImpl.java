package com.saleshub.service.impl;

import com.saleshub.entity.SysAuditLog;
import com.saleshub.mapper.SysAuditLogMapper;
import com.saleshub.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final SysAuditLogMapper auditLogMapper;

    @Override
    @Async
    public void log(Long userId, String username, String action, String targetType, Long targetId, String detail) {
        SysAuditLog log = new SysAuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}
