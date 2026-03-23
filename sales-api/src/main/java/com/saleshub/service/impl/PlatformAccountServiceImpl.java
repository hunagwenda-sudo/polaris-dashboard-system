package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.entity.SysPlatformAccount;
import com.saleshub.mapper.SysPlatformAccountMapper;
import com.saleshub.service.PlatformAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAccountServiceImpl implements PlatformAccountService {

    private final SysPlatformAccountMapper accountMapper;

    @Override
    public List<SysPlatformAccount> listByPlatform(String platformCode) {
        return accountMapper.selectList(
            new LambdaQueryWrapper<SysPlatformAccount>()
                .eq(SysPlatformAccount::getPlatformCode, platformCode)
                .orderByAsc(SysPlatformAccount::getSort)
        );
    }

    @Override
    public List<SysPlatformAccount> listAll() {
        return accountMapper.selectList(
            new LambdaQueryWrapper<SysPlatformAccount>()
                .orderByAsc(SysPlatformAccount::getPlatformCode)
                .orderByAsc(SysPlatformAccount::getSort)
        );
    }

    @Override
    public SysPlatformAccount create(SysPlatformAccount account) {
        log.info("创建平台账号: platform={}, name={}", account.getPlatformCode(), account.getAccountName());
        if (account.getStatus() == null) account.setStatus("active");
        if (account.getSort() == null) account.setSort(0);
        account.setCreatedAt(LocalDateTime.now());
        accountMapper.insert(account);
        return account;
    }

    @Override
    public SysPlatformAccount update(Long id, SysPlatformAccount account) {
        log.info("更新平台账号: id={}", id);
        SysPlatformAccount existing = accountMapper.selectById(id);
        if (existing == null) throw new BusinessException("账号不存在");
        if (account.getAccountName() != null) existing.setAccountName(account.getAccountName());
        if (account.getSort() != null) existing.setSort(account.getSort());
        if (account.getStatus() != null) existing.setStatus(account.getStatus());
        accountMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        log.info("删除平台账号: id={}", id);
        SysPlatformAccount existing = accountMapper.selectById(id);
        if (existing == null) throw new BusinessException("账号不存在");
        accountMapper.deleteById(id);
    }
}
