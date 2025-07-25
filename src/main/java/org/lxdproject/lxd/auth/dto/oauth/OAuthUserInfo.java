package org.lxdproject.lxd.auth.dto.oauth;

import org.lxdproject.lxd.member.entity.enums.LoginType;

public interface OAuthUserInfo {
    String getEmail();
    String getName();
    String getProfileImage();

    LoginType getLoginType();
}
