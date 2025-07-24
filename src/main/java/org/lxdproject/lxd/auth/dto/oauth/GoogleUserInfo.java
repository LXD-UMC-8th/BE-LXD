package org.lxdproject.lxd.auth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleUserInfo implements OAuthUserInfo{

    private String id;
    private String email;
    private String name;

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
}
