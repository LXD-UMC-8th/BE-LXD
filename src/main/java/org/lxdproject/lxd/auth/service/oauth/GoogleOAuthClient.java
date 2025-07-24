package org.lxdproject.lxd.auth.service.oauth;

import org.lxdproject.lxd.auth.dto.oauth.OAuthUserInfo;

public class GoogleOAuthClient implements OAuthClient {
    @Override
    public String requestAccessToken(String code) {
        return "";
    }

    @Override
    public OAuthUserInfo requestUserInfo(String accessToken) {
        return null;
    }
}
