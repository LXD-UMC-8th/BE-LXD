package org.lxdproject.lxd.auth.converter;

import org.lxdproject.lxd.auth.dto.AuthResponseDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;

public class AuthConverter {

    public static AuthResponseDTO.LoginResponseDTO toLoginResponseDTO(String accessToken, Member member) {
        return AuthResponseDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .member(AuthResponseDTO.LoginResponseDTO.MemberDTO.builder()
                        .memberId(member.getId())
                        .email(member.getEmail())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImg(member.getProfileImg())
                        .language(member.getLanguage().name())
                        .build())
                .build();
    }

}
