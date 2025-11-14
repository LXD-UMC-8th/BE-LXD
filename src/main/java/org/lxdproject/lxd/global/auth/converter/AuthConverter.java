package org.lxdproject.lxd.global.auth.converter;

import org.lxdproject.lxd.global.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.domain.member.entity.Member;

public class AuthConverter {

    public static AuthResponseDTO.LoginResponseDTO toLoginResponseDTO(String accessToken, String refreshToken, Member member, Boolean isWithDrawn) {
        return AuthResponseDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(AuthResponseDTO.LoginResponseDTO.MemberDTO.builder()
                        .memberId(member.getId())
                        .email(member.getEmail())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImg(member.getProfileImg())
                        .nativeLanguage(member.getNativeLanguage().name())
                        .studyLanguage(member.getLanguage().name())
                        .build())
                .isWithdrawn(isWithDrawn)
                .build();
    }

}
