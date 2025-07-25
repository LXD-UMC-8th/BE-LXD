package org.lxdproject.lxd.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member join(MemberRequestDTO.JoinRequestDTO joinRequestDTO) {


        if(!joinRequestDTO.getIsPrivacyAgreed().equals(Boolean.TRUE)){
            throw new MemberHandler(ErrorStatus.PRIVACY_POLICY_NOT_AGREED);
        }

        if(memberRepository.existsByEmail(joinRequestDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        if(memberRepository.existsByNickname(joinRequestDTO.getNickname())) {
            throw new MemberHandler(ErrorStatus.NICKNAME_DUPLICATION);
        }

        Member member = MemberConverter.toMember(joinRequestDTO, passwordEncoder.encode(joinRequestDTO.getPassword()));

        memberRepository.save(member);
        return member;

    }
}
