package org.lxdproject.lxd.global.auth.service.oauthClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.auth.dto.oauth.GoogleTokenResponse;
import org.lxdproject.lxd.global.auth.dto.oauth.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
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
        // 디코드
        String decoded = URLDecoder.decode(code, StandardCharsets.UTF_8);


        HttpHeaders headers = new HttpHeaders();
        // 요청 헤더: x-www-form-urlencoded 형식 지정 (필수)
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디: Map이 아닌 MultiValueMap 이여야 Http 전송 가능
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", decoded);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        // 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // code 처리 중 발생하는 에러 처리
        try {
            ResponseEntity<GoogleTokenResponse> response =
                    restTemplate.postForEntity(accessTokenUrl, request, GoogleTokenResponse.class);

            return Optional.ofNullable(response.getBody())
                    .map(GoogleTokenResponse::getAccessToken)
                    .orElseThrow(() -> new AuthHandler(ErrorStatus.INVALID_GOOGLE_AUTH_CODE));

        } catch (HttpClientErrorException e) {
            // 응답 바디까지 로그로 확인
            log.error("[TOKEN ERR] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }

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
