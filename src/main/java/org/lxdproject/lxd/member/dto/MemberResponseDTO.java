package org.lxdproject.lxd.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.diary.entity.enums.Language;

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

            @Schema(description = "선택 언어 (KO, ENG 등)")
            private String language;
        }


    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "언어 변경 응답 DTO")
    public static class UpdateLanguageResponseDTO {
        @Schema(description = "학습 언어 (예: KO, ENG)")
        private Language language;
    }



}
