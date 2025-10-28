package org.lxdproject.lxd.unit.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.lxdproject.lxd.auth.enums.TokenType;
import org.lxdproject.lxd.auth.service.AuthService;
import org.lxdproject.lxd.config.properties.UrlProperties;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.lxdproject.lxd.infra.mail.MailService;
import org.lxdproject.lxd.infra.redis.RedisService;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login 테스트")
    class Login{

        private AuthRequestDTO.LoginRequestDTO loginRequest;
        private CustomUserDetails customUserDetails;
        private Authentication authentication;

        @BeforeEach
        void setUp() {

            // 로그인 요청 DTO
            loginRequest = new AuthRequestDTO.LoginRequestDTO(
                    "test@naver.com",
                    "1234"
            );

            // CustomUserDetails Mock 생성
            customUserDetails = mock(CustomUserDetails.class);

            when(customUserDetails.getMemberId()).thenReturn(1L);
            when(customUserDetails.getMemberEmail()).thenReturn("test@naver.com");
            when(customUserDetails.getRole()).thenReturn(Role.USER);
            when(customUserDetails.getMember()).thenReturn(Member.builder()
                    .id(1L)
                    .email("test@naver.com")
                    .nativeLanguage(Language.KO)
                    .language(Language.ENG)
                    .systemLanguage(Language.KO)
                    .isPurged(Boolean.FALSE)
                    .build()
            );

            // Authentication Mock 생성
            authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);

            // AuthenticationManager 동작 세팅
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString(), eq(TokenType.ACCESS)))
                    .thenReturn("access_token");
            when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString(), eq(TokenType.REFRESH)))
                    .thenReturn("refresh_token");

        }

        @Test
        @DisplayName("정상 로그인 시 access/refresh 토큰 및 redis 토큰 생성 검사")
        void login_verify_token(){

            // when
            AuthResponseDTO.LoginResponseDTO loginResponseDTO = authService.login(loginRequest);

            // then
            verify(jwtTokenProvider).generateToken(anyLong(), anyString(), eq(Role.USER.name()), eq(TokenType.ACCESS));
            verify(jwtTokenProvider).generateToken(anyLong(), anyString(), eq(Role.USER.name()), eq(TokenType.REFRESH));

            assertThat(loginResponseDTO.getAccessToken()).isEqualTo("access_token");
            assertThat(loginResponseDTO.getRefreshToken()).isEqualTo("refresh_token");

            verify(redisService).setRefreshToken(eq("refresh_token"), eq("test@naver.com"), eq(Duration.ofDays(7)));

        }
    }

}