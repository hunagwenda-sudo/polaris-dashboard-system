package com.saleshub.service;

import com.saleshub.entity.SysPlatformAccount;
import java.util.List;

public interface PlatformAccountService {
    List<SysPlatformAccount> listByPlatform(String platformCode);
    List<SysPlatformAccount> listAll();
    SysPlatformAccount create(SysPlatformAccount account);
    SysPlatformAccount update(Long id, SysPlatformAccount account);
    void delete(Long id);
}
