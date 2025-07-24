package org.lxdproject.lxd.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseDTO{

        @Schema(description = "액세스 토큰")
        private String accessToken;

        // TODO refresh 토큰 구현 시 해당 부분 주석 제거하기
        /*@Schema(description = "리프레쉬 토큰")
        private String refreshToken;*/

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

            @Schema(description = "선택 언어 (KO, ENG 등)")
            private String language;
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

        // TODO refresh 토큰 구현 시 해당 부분 주석 제거하기
        /*@Schema(description = "리프레쉬 토큰")
        private String refreshToken;*/

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

            @Schema(description = "선택 언어 (KO, ENG 등)")
            private String language;
        }


    }

}
