package org.lxdproject.lxd.member.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.dto.LanguageChangeResponseDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingRequestDTO;
import org.lxdproject.lxd.member.dto.LanguageSettingResponseDTO;
import org.lxdproject.lxd.member.entity.Member;

import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LanguageSettingService {
    private final MemberRepository memberRepository;

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
    public LanguageChangeResponseDTO setLanguage(Long memberId, LanguageSettingRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateSystemLanguage(request.getSystemLanguage());

        LanguageChangeResponseDTO response = new LanguageChangeResponseDTO();
        response.setSystemLanguage(member.getSystemLanguage());

        return response;
    }


}
