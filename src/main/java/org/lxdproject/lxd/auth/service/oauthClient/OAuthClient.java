package org.lxdproject.lxd.auth.service.oauthClient;

import org.lxdproject.lxd.auth.dto.oauth.OAuthUserInfo;

public interface OAuthClient {
    String requestAccessToken(String code);
    OAuthUserInfo requestUserInfo(String accessToken);
}
