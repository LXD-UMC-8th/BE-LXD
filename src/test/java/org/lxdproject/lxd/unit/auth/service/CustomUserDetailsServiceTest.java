package org.lxdproject.lxd.unit.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.lxdproject.lxd.auth.service.CustomUserDetailsService;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("loadUserByUsername 테스트")
    class LoadUserByUsername {

        Member member;

        @BeforeEach
        void setUp() {

            member = Member.builder()
                    .id(1L)
                    .email("user@test.com")
                    .username("user")
                    .password("1234")
                    .role(Role.USER)
                    .nativeLanguage(Language.KO)
                    .language(Language.ENG)
                    .systemLanguage(Language.KO)
                    .isPrivacyAgreed(true)
                    .isAlarmAgreed(true)
                    .loginType(LoginType.LOCAL)
                    .build();


        }

    }

}
