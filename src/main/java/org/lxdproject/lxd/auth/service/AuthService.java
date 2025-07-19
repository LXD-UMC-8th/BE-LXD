package org.lxdproject.lxd.auth.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.converter.AuthConverter;
import org.lxdproject.lxd.auth.dto.AuthRequestDTO;
import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
    }
}
