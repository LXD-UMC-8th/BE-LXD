package org.lxdproject.lxd.auth.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.converter.AuthConverter;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.config.properties.UrlProperties;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.lxdproject.lxd.infra.mail.MailService;
import org.lxdproject.lxd.infra.redis.RedisService;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
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

        // 인증 완료 후, 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(member.getId(), member.getEmail(), member.getRole().name());

        return AuthConverter.toLoginResponseDTO(
                accessToken,
                member
        );
    }

    public void sendVerificationEmail(AuthRequestDTO.@Valid sendVerificationRequestDTO sendVerificationRequestDTO) {

        // 이미 존재하는 이메일인지 유효성 검사
        if(memberRepository.existsByEmail(sendVerificationRequestDTO.getEmail()).equals(Boolean.TRUE)){
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        // 토큰 생성 및 인증 링크 구성
        String token = createSecureToken();
        String title = "LXD 이메일 인증 번호";
        String verificationLink = urlProperties.getBackend() + "/auth/emails/verifications?token=" + token;

        // 이메일 발송
        mailService.sendEmail(sendVerificationRequestDTO.getEmail(), title, verificationLink);

        // Redis에 기존 값 삭제 후 재등록
        redisService.deleteValues(sendVerificationRequestDTO.getEmail());
        redisService.setValues(sendVerificationRequestDTO.getEmail(), token, Duration.ofMinutes(5L));
    }

    private String createSecureToken() {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }
}
