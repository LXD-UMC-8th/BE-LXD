package org.lxdproject.lxd.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.converter.AuthConverter;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.dto.oauth.OAuthUserInfo;
import org.lxdproject.lxd.auth.enums.TokenType;
import org.lxdproject.lxd.config.properties.UrlProperties;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.lxdproject.lxd.infra.mail.MailService;
import org.lxdproject.lxd.infra.redis.RedisService;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.net.URLEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final MailService mailService;
    private final UrlProperties urlProperties;
    private final RedisService redisService;

    public AuthResponseDTO.LoginResponseDTO login(AuthRequestDTO.LoginRequestDTO loginRequestDTO) {

        // 아이디 검사
        Member member = memberRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(()-> new MemberHandler(ErrorStatus.INVALID_CREDENTIALS));

        // 비밀번호 검사
        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword())) {
            throw new MemberHandler(ErrorStatus.INVALID_CREDENTIALS);
        }

        // 일반 로그인인지 검사
        if(member.getLoginType() != LoginType.LOCAL) {
            throw new MemberHandler(ErrorStatus.INVALID_CREDENTIALS);
        }

        // 인증 완료 후, 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.ACCESS);
        String refreshToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.REFRESH);

        redisService.setValues(refreshToken, member.getEmail(), Duration.ofDays(7L));

        return AuthConverter.toLoginResponseDTO(
                accessToken,
                refreshToken,
                member
        );
    }

    public void sendVerificationEmail(AuthRequestDTO.sendVerificationRequestDTO sendVerificationRequestDTO) {

        // 이미 존재하는 이메일인지 유효성 검사
        if (memberRepository.existsByEmail(sendVerificationRequestDTO.getEmail()).equals(Boolean.TRUE)) {
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        // 토큰 생성 및 인증 링크 구성
        String token = createSecureToken();
        String title = "LXD 이메일 인증 번호";
        String verificationLink = urlProperties.getBackend() + "/auth/emails/verifications?token=" + token;

        boolean htmlSent = false;

        // HTML 형식으로 이메일 전송
        try {
            Resource resource = new ClassPathResource("templates/email.html");
            String htmlTemplate = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String htmlContent = htmlTemplate.replace("{{verificationLink}}", verificationLink);
            mailService.sendEmailFromMimeMessage(sendVerificationRequestDTO.getEmail(), title, htmlContent);
            htmlSent = true;
        } catch (IOException e) {
            log.error("이메일 템플릿 로드 실패", e);
        }

        // HTML 전송 실패 시 텍스트 메일로 fallback
        if (!htmlSent) {
            String text = "아래 링크를 눌러 이메일 인증을 완료해주세요.\n" +
                    "5분간 유효합니다.\n\n" +
                    verificationLink;
            mailService.sendEmail(sendVerificationRequestDTO.getEmail(), title, text);
        }

        // Redis에 기존 값 삭제 후 재등록
        // TODO 현재 같은 이메일 요청을 여러번 할 시, 한 개의 이메일에 여러 개의 토큰이 존재함 -> Redis hash 방식으로 추후 refactoring 하기
        redisService.deleteValues(token);
        redisService.setValues(token, sendVerificationRequestDTO.getEmail(), Duration.ofMinutes(5L));
    }

    private String createSecureToken() {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }

    public void verifyEmailTokenAndRedirect(String token, HttpServletResponse response) {
        String email = redisService.getValues(token);
        try {
            if (email == null) {
                // 만료 또는 잘못된 토큰일 경우 → 실패 페이지로
                response.sendRedirect(urlProperties.getFrontend() + "/email-verification/fail");
            } else {
                redisService.deleteValues(token); // 재사용 방지

                String newToken = createSecureToken();
                redisService.setValues(newToken, email, Duration.ofMinutes(3L));

                // +,/ 등이 포함될 수 있어 잘못 해석될 여지 방지
                String encoded = URLEncoder.encode(newToken, UTF_8);
                response.sendRedirect(urlProperties.getFrontend() + "/home/signup?token=" + encoded);
            }
        }catch (IOException e) {
            log.error("redirect에 실패했습니다");
            throw new RuntimeException("리다이렉트 실패", e);
        }

    }


    public AuthResponseDTO.SocialLoginResponseDTO socialLogin(OAuthUserInfo oAuthUserInfo) {


        String email = oAuthUserInfo.getEmail();
        Member member = memberRepository.findByEmail(email).orElse(null);

        System.out.println(member);

        // 새로운 유저 -> 회원가입 페이지로 이동시키기
        if(member == null) {
            return AuthResponseDTO.SocialLoginResponseDTO.builder()
                    .isNewMember(Boolean.TRUE) // 새로운 유저
                    .accessToken(null)
                    .member(AuthResponseDTO.SocialLoginResponseDTO.MemberDTO.builder()
                            .email(email)
                            .loginType(oAuthUserInfo.getLoginType()) // 로그인 방법도 response에 포함
                            .build())
                    .build();
        }

        // 기존 유저 -> 로그인 후 액세스 토큰 및 리프레쉬 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.ACCESS);
        String refreshToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.REFRESH);

        // redis에 refreshToken 저장
        redisService.setValues(refreshToken, member.getEmail(), Duration.ofDays(7L));

        return AuthResponseDTO.SocialLoginResponseDTO.builder()
                .isNewMember(Boolean.FALSE) // 기존 유저
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(AuthResponseDTO.SocialLoginResponseDTO.MemberDTO.builder()
                        .memberId(member.getId())
                        .email(member.getEmail())
                        .username(member.getUsername())
                        .profileImg(member.getProfileImg())
                        .nickname(member.getNickname())
                        .nativeLanguage(member.getNativeLanguage().name())
                        .studyLanguage(member.getLanguage().name())
                        .loginType(oAuthUserInfo.getLoginType())
                        .build())
                .build();

    }

    public AuthResponseDTO.ReissueResponseDTO reissue(AuthRequestDTO.@Valid ReissueRequestDTO reissueRequestDTO) {

        String refreshToken = reissueRequestDTO.getRefreshToken();

        // refresh 토큰 유효성 검사
        jwtTokenProvider.validateRefreshTokenOrThrow(refreshToken);

        String email = redisService.getValues(refreshToken);

        if(email == null) {
            throw new AuthHandler(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));


        String newAccessToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.ACCESS);
        String newRefreshToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name(), TokenType.REFRESH);

        redisService.deleteValues(refreshToken);
        redisService.setValues(newRefreshToken, member.getEmail(), Duration.ofDays(7L));

        return new AuthResponseDTO.ReissueResponseDTO().builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

    }

    public void logout(AuthRequestDTO.LogoutRequestDTO logoutRequestDTO) {

        String refreshToken = logoutRequestDTO.getRefreshToken();

        // refresh 토큰 유효성 검사
        jwtTokenProvider.validateRefreshTokenOrThrow(refreshToken);

        String email = redisService.getValues(refreshToken);

        if(email == null) {
            throw new AuthHandler(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        redisService.deleteValues(refreshToken);

    }

    public AuthResponseDTO.GetEmailByTokenResponseDTO getEmailByToken(String token) {

        String email = redisService.getValues(token);

        if(email == null) {
            throw new AuthHandler(ErrorStatus.INVALID_EMAIL_TOKEN);
        }

        return AuthResponseDTO.GetEmailByTokenResponseDTO.builder()
                .email(email)
                .build();
    }
}
