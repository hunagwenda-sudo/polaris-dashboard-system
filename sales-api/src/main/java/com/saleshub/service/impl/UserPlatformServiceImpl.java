package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.entity.SysPlatformAccount;
import com.saleshub.entity.SysUserPlatform;
import com.saleshub.mapper.SysPlatformAccountMapper;
import com.saleshub.mapper.SysUserPlatformMapper;
import com.saleshub.service.UserPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPlatformServiceImpl implements UserPlatformService {

    private final SysUserPlatformMapper userPlatformMapper;
    private final SysPlatformAccountMapper accountMapper;

    @Override
    public List<SysUserPlatform> listByUserId(Long userId) {
        return userPlatformMapper.selectList(
            new LambdaQueryWrapper<SysUserPlatform>()
                .eq(SysUserPlatform::getUserId, userId)
        );
    }

    @Override
    @Transactional
    public void assign(Long userId, List<Long> accountIds) {
        // 查出当前分配
        List<SysUserPlatform> existing = listByUserId(userId);
        Set<Long> existingIds = existing.stream().map(SysUserPlatform::getAccountId).collect(Collectors.toSet());
        Set<Long> newIds = Set.copyOf(accountIds);

        // 删除不再需要的
        for (SysUserPlatform up : existing) {
            if (!newIds.contains(up.getAccountId())) {
                userPlatformMapper.deleteById(up.getId());
            }
        }

        // 新增缺少的
        if (!accountIds.isEmpty()) {
            // 批量查 account 获取 platformCode
            List<SysPlatformAccount> accounts = accountMapper.selectList(
                new LambdaQueryWrapper<SysPlatformAccount>().in(SysPlatformAccount::getId, accountIds)
            );
            Map<Long, String> idToPlatform = accounts.stream()
                .collect(Collectors.toMap(SysPlatformAccount::getId, SysPlatformAccount::getPlatformCode));

            for (Long accountId : accountIds) {
                if (!existingIds.contains(accountId)) {
                    SysUserPlatform up = new SysUserPlatform();
                    up.setUserId(userId);
                    up.setAccountId(accountId);
                    up.setPlatformCode(idToPlatform.getOrDefault(accountId, ""));
                    userPlatformMapper.insert(up);
                }
            }
        }
    }
}
