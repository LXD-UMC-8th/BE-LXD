package org.lxdproject.lxd.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.member.entity.enums.LoginType;

public class AuthResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseDTO{

        @Schema(description = "액세스 토큰")
        private String accessToken;

        @Schema(description = "리프레쉬 토큰")
        private String refreshToken;

        @Schema(description = "탈퇴한 사용자인지 여부")
        private Boolean isWithdrawn;

        @Schema(description = "로그인한 멤버 정보")
        private MemberDTO member;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MemberDTO {
            @Schema(description = "멤버 고유 번호")
            private Long memberId;

            @Schema(description = "이메일")
            private String email;

            @Schema(description = "아이디")
            private String username;

            @Schema(description = "닉네임")
            private String nickname;

            @Schema(description = "프로필 이미지 URL")
            private String profileImg;

            @Schema(description = "주 사용 언어", example = "KO")
            private String nativeLanguage;

            @Schema(description = "학습 언어", example = "ENG")
            private String studyLanguage;

        }


    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLoginResponseDTO {

        @Schema(description = "새로운 유저 여부")
        private Boolean isNewMember;

        @Schema(description = "액세스 토큰")
        private String accessToken;

        @Schema(description = "리프레쉬 토큰")
        private String refreshToken;

        @Schema(description = "탈퇴한 사용자인지 여부")
        private Boolean isWithdrawn;

        @Schema(description = "로그인한 멤버 정보")
        private MemberDTO member;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MemberDTO {
            @Schema(description = "멤버 고유 번호")
            private Long memberId;

            @Schema(description = "이메일")
            private String email;

            @Schema(description = "아이디")
            private String username;

            @Schema(description = "닉네임")
            private String nickname;

            @Schema(description = "프로필 이미지 URL")
            private String profileImg;

            @Schema(description = "주 사용 언어", example = "KO")
            private String nativeLanguage;

            @Schema(description = "학습 언어", example = "ENG")
            private String studyLanguage;

            // 새로운 소셜 로그인 시, 회원가입을 위해 로그인 방법 정보 제공
            @Schema(description = "로그인 방법 (ex GOOGLE)")
            private LoginType loginType;
        }


    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReissueResponseDTO {

        @Schema(description = "access token")
        private String accessToken;

        @Schema(description = "refresh token")
        private String refreshToken;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetEmailByTokenResponseDTO {

        @Schema(description = "이메일")
        private String email;

    }

}
