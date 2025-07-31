package org.lxdproject.lxd.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MemberResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResponseDTO{

        @Schema(description = "회원가입한 멤버 정보")
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

            @Schema(description = "주 사용언어", example = "KO")
            private String nativeLanguage;

            @Schema(description = "학습 언어", example = "ENG")
            private String studyLanguage;
        }
    }

    @Builder
    @Getter
    public static class MemberInfoDTO{
        private Long memberId;
        private String username; // 아이디
        private String email; // 가입 이메일
        private String nickname;
        private String profileImg;
    }

    @Builder
    @Getter
    public static class CheckUsernameResponseDTO {
        private String username;
        private boolean isDuplicated;
    }

}
