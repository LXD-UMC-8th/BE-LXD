package org.lxdproject.lxd.global.auth.service.oauthClient;

import org.lxdproject.lxd.global.auth.dto.oauth.OAuthUserInfo;

public interface OAuthClient {
    String requestAccessToken(String code);
    OAuthUserInfo requestUserInfo(String accessToken);
}
