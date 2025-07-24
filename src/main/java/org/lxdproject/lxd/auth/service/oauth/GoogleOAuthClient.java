package org.lxdproject.lxd.auth.service.oauth;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.auth.dto.oauth.GoogleTokenResponse;
import org.lxdproject.lxd.auth.dto.oauth.GoogleUserInfo;
import org.lxdproject.lxd.auth.dto.oauth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    private final String accessTokenUrl = "https://oauth2.googleapis.com/token";
    private final String userInfoUrl = "https://www.googleapis.com/userinfo/v2/me";


    @Override
    public String requestAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put(code, URLDecoder.decode(code, StandardCharsets.UTF_8));
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("redirect_uri", redirectUri);
        body.put("grant_type", "authorization_code");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(accessTokenUrl, request, GoogleTokenResponse.class);

        return Optional.ofNullable(response.getBody())
                .map(GoogleTokenResponse::getAccessToken)
                .orElseThrow(() -> new RuntimeException("구글 AccessToken 요청 실패"));

    }

    @Override
    public GoogleUserInfo requestUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, GoogleUserInfo.class);

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new RuntimeException("구글 사용자 정보 요청 실패"));
    }
}
