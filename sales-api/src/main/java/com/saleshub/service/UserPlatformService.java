package com.saleshub.service;

import com.saleshub.entity.SysUserPlatform;
import java.util.List;

public interface UserPlatformService {
    /** 获取用户的渠道分配列表 */
    List<SysUserPlatform> listByUserId(Long userId);

    /** 全量替换用户的渠道分配（传入 accountId 列表） */
    void assign(Long userId, List<Long> accountIds);
}
