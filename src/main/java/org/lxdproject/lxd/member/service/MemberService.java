package org.lxdproject.lxd.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.ImageResponseDTO;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.dto.MemberResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public Member join(MemberRequestDTO.JoinRequestDTO joinRequestDTO, MultipartFile profileImg) {


        if(!joinRequestDTO.getIsPrivacyAgreed().equals(Boolean.TRUE)){
            throw new MemberHandler(ErrorStatus.PRIVACY_POLICY_NOT_AGREED);
        }

        if(memberRepository.existsByEmail(joinRequestDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        if(memberRepository.existsByUsername(joinRequestDTO.getUsername())) {
            throw new MemberHandler(ErrorStatus.USERNAME_DUPLICATION);
        }

        if(memberRepository.existsByNickname(joinRequestDTO.getNickname())) {
            throw new MemberHandler(ErrorStatus.NICKNAME_DUPLICATION);
        }

        String profileImgURL  = null;
        Member member = null;

        // 프로필 이미지가 있는 경우
        if(profileImg != null && !profileImg.isEmpty()) {
            profileImgURL = imageService.uploadImage(profileImg, ImageDir.PROFILE).getImageUrl();
            member = MemberConverter.toMember(joinRequestDTO, profileImgURL ,passwordEncoder.encode(joinRequestDTO.getPassword()));
        }else{
            member = MemberConverter.toMember(joinRequestDTO, profileImgURL ,passwordEncoder.encode(joinRequestDTO.getPassword()));
        }

        memberRepository.save(member);

        return member;

    }

    public MemberResponseDTO.MemberInfoDTO getMemberInfo() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return MemberResponseDTO.MemberInfoDTO.builder()
                .memberId(currentMemberId)
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImg(member.getProfileImg())
                .build();
    }

    public MemberResponseDTO.CheckUsernameResponseDTO isUsernameDuplicated(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new MemberHandler(ErrorStatus.INVALID_USERNAME);
        }
        Boolean exists = memberRepository.existsByUsername(username);

        return MemberResponseDTO.CheckUsernameResponseDTO.builder()
                .username(username)
                .isDuplicated(exists)
                .build();
    }
}
