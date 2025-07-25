package org.lxdproject.lxd.auth.service.oauth;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.auth.dto.oauth.GoogleTokenResponse;
import org.lxdproject.lxd.auth.dto.oauth.GoogleUserInfo;
import org.lxdproject.lxd.auth.dto.oauth.OAuthUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        // 요청 헤더: x-www-form-urlencoded 형식 지정 (필수)
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디: Map이 아닌 MultiValueMap 이여야 Http 전송 가능
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        // 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // 구글에 POST 요청 → 액세스 토큰 응답 받기
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
