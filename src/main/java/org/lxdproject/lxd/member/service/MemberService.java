package org.lxdproject.lxd.member.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.entity.enums.ImageDir;
import org.lxdproject.lxd.common.service.ImageService;
import org.lxdproject.lxd.common.util.S3Uploader;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.*;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final S3Uploader s3Uploader;

    public Member join(MemberRequestDTO.JoinRequestDTO joinRequestDTO, MultipartFile profileImg) {


        if (!joinRequestDTO.getIsPrivacyAgreed().equals(Boolean.TRUE)) {
            throw new MemberHandler(ErrorStatus.PRIVACY_POLICY_NOT_AGREED);
        }

        if (memberRepository.existsByEmail(joinRequestDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        if (memberRepository.existsByUsername(joinRequestDTO.getUsername())) {
            throw new MemberHandler(ErrorStatus.USERNAME_DUPLICATION);
        }

        String profileImgURL = null;
        Member member = null;

        // 프로필 이미지가 있는 경우
        if (profileImg != null && !profileImg.isEmpty()) {
            profileImgURL = imageService.uploadImage(profileImg, ImageDir.PROFILE).getImageUrl();
            member = MemberConverter.toMember(joinRequestDTO, profileImgURL, passwordEncoder.encode(joinRequestDTO.getPassword()));
        } else {
            member = MemberConverter.toMember(joinRequestDTO, profileImgURL, passwordEncoder.encode(joinRequestDTO.getPassword()));
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

    @Transactional
    public MemberResponseDTO.MemberInfoDTO updateMemberInfo(
            MemberRequestDTO.ProfileUpdateDTO profileUpdateDTO,
            MultipartFile profileImg
    ) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (profileUpdateDTO != null) {
            String newNickname = profileUpdateDTO.getNickname();
            if (newNickname != null && !newNickname.equals(member.getNickname())) {
                if (!StringUtils.hasText(newNickname)) {
                    throw new MemberHandler(ErrorStatus.INVALID_NICKNAME);
                }
                member.setNickname(newNickname);
            }
        }

        if (profileImg != null && !profileImg.isEmpty()) {
            String newImageUrl = imageService.uploadImage(profileImg, ImageDir.PROFILE)
                    .getImageUrl();
            // 기존 이미지 삭제
            String oldImageUrl = member.getProfileImg();
            if (StringUtils.hasText(oldImageUrl)) {
                s3Uploader.deleteFiles(List.of(oldImageUrl));
            }
            // 새 이미지 업로드
            member.setProfileImg(newImageUrl);
        }

        return MemberResponseDTO.MemberInfoDTO.builder()
                .memberId(currentMemberId)
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImg(member.getProfileImg())
                .build();
    }

    @Transactional(readOnly = true)
    public LanguageSettingResponseDTO getLanguageSetting(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return LanguageSettingResponseDTO.builder()
                .nativeLanguage(member.getNativeLanguage())
                .studyLanguage(member.getLanguage())
                .systemLanguage(member.getSystemLanguage())
                .build();
    }

    @Transactional
    public LanguageChangeResponseDTO setSystemLanguage(Long memberId, LanguageSettingRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateSystemLanguage(request.getSystemLanguage());

        return LanguageChangeResponseDTO.builder()
                .systemLanguage(member.getSystemLanguage())
                .build();
    }

    @Transactional
    public void setPasswordSetting(MemberRequestDTO.SetPasswordSettingRequestDTO setPasswordSettingRequestDTO) {

        // 새 비밀번호와 새 비밀번호 확인이 일치하지 않을 경우
        if(!setPasswordSettingRequestDTO.getNewPassword().equals(setPasswordSettingRequestDTO.getConfirmNewPassword())){
            throw new MemberHandler(ErrorStatus.NEW_PASSWORDS_DO_NOT_MATCH);
        }

        Member member = memberRepository.findByEmail(setPasswordSettingRequestDTO.getEmail())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updatePassword(passwordEncoder.encode(setPasswordSettingRequestDTO.getNewPassword()));

    }
}
