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

        @Test
        @DisplayName("회원이 존재하면 CustomUserDetails 반환")
        void loadUserByUsername_returnsCustomUserDetails() {
            // given
            when(memberRepository.findByEmail(member.getEmail()))
                    .thenReturn(Optional.of(member));

            // when
            UserDetails result = customUserDetailsService.loadUserByUsername(member.getEmail());

            // then
            assertThat(result).isInstanceOf(CustomUserDetails.class);
            CustomUserDetails details = (CustomUserDetails) result;
            assertThat(details.getMemberEmail()).isEqualTo(member.getEmail());
            assertThat(details.getMemberId()).isEqualTo(member.getId());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 UsernameNotFoundException 발생")
        void loadUserByUsername_returnsUsernameNotFoundException() {

            // given
            when(memberRepository.findByEmail("unknown@naver.com"))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    customUserDetailsService.loadUserByUsername("unknown@naver.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("해당하는 유저를 찾을 수 없습니다.");

        }


    }

}
