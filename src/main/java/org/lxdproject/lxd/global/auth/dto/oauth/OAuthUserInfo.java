package org.lxdproject.lxd.global.auth.dto.oauth;

import org.lxdproject.lxd.domain.member.entity.enums.LoginType;

public interface OAuthUserInfo {
    String getEmail();
    String getName();
    String getProfileImage();

    LoginType getLoginType();
}
