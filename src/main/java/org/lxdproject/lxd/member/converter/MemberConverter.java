package org.lxdproject.lxd.member.converter;

import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.entity.enums.Status;

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

    public static Member toMember(MemberRequestDTO.JoinRequestDTO joinRequestDTO, String encryptedPassword) {
        return Member.builder()
                .nativeLanguage(joinRequestDTO.getNativeLanguage())
                .language(joinRequestDTO.getLanguage())
                .role(Role.USER)
                .username(joinRequestDTO.getUsername())
                .password(encryptedPassword)
                .email(joinRequestDTO.getEmail())
                .nickname(joinRequestDTO.getNickname())
                .loginType(LoginType.LOCAL)
                .isPrivacyAgreed(joinRequestDTO.getIsPrivacyAgreed())
                .profileImg(joinRequestDTO.getProfileImg())
                .status(Status.ACTIVE)
                .isAlarmAgreed(Boolean.FALSE) // 알람은 꺼져있는게 Default
                .build();

    }

}
