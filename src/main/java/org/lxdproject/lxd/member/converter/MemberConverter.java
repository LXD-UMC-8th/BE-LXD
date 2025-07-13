package org.lxdproject.lxd.member.converter;

import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;

public class MemberConverter {

    public static MemberResponseDTO.JoinResponseDTO toJoinResponseDTO(Member member) {
        return MemberResponseDTO.JoinResponseDTO.builder()
                .member(MemberResponseDTO.JoinResponseDTO.MemberDTO.builder()
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
