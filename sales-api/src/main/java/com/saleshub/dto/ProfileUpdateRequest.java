package com.saleshub.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String phone;
    private String avatar;
    private String oldPassword;
    private String newPassword;
    private Boolean forceChange; // 首次登录强制修改密码，跳过旧密码校验
}
