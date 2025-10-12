package org.lxdproject.lxd.auth.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. 아이디(이메일) 검사
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다.: " + username));

        // 2. 조회된 Member 엔티티를 바탕으로 CustomUserDetails 객체를 생성하여 반환
        return new CustomUserDetails(member);
    }
}
