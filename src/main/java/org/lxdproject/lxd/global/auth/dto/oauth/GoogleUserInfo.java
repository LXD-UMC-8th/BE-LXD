package org.lxdproject.lxd.global.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.lxdproject.lxd.domain.member.entity.enums.LoginType;

public class GoogleUserInfo implements OAuthUserInfo{

    private String id;
    private String email;
    private String name;

    private final LoginType provider = LoginType.GOOGLE;

    @JsonProperty("picture")
    private String profileImage;

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProfileImage() {
        return profileImage;
    }

    @Override
    public LoginType getLoginType() {
        return provider;
    }
}
