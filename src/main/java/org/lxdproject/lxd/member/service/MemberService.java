package org.lxdproject.lxd.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.converter.MemberConverter;
import org.lxdproject.lxd.member.dto.MemberRequestDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    // private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Member join(MemberRequestDTO.JoinRequestDTO joinRequestDTO) {

        System.out.print("email : " + joinRequestDTO.getEmail());

        if(memberRepository.existsByEmail(joinRequestDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus.EMAIL_DUPLICATION);
        }

        if(memberRepository.existsByNickname(joinRequestDTO.getNickname())) {
            throw new MemberHandler(ErrorStatus.NICKNAME_DUPLICATION);
        }

        // TODO Spring Security 의존성 추가 시, 해당 Encoder을 사용해서 비밀번호 암호화 하도록 변경하기
        // Member member = MemberConverter.toMember(joinRequestDTO, bCryptPasswordEncoder.encode(joinRequestDTO.getPassword()));
        Member member = MemberConverter.toMember(joinRequestDTO, "1234");

        memberRepository.save(member);
        return member;

    }
}
