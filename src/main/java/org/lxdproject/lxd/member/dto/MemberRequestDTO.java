package org.lxdproject.lxd.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.lxdproject.lxd.diary.entity.enums.Language;

import java.time.LocalDateTime;
import java.util.List;

public class MemberRequestDTO {

    @Getter
    @Schema(description = "회원가입을 위한 RequestDTO")
    public static class JoinRequestDTO{

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식을 지켜야합니다.")
        String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password;

        @NotNull(message = "개인정보 수집 및 이용 동의는 필수입니다.")
        private Boolean isPrivacyAgreed;

        @NotBlank(message = "아이디는 필수입니다.")
        @Size(max = 20, message = "아이디는 최대 20자까지 가능합니다.")
        String username;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
        String nickname;

        String profileImg;

        // 주사용언어
        @NotNull(message = "주 사용언어는 필수입니다.")
        @Schema(description = "사용 언어 (예: ENG, KO)")
        private Language nativeLanguage;

        // 학습언어
        @NotNull(message = "학습언어는 필수입니다.")
        @Schema(description = "사용 언어 (예: ENG, KO)")
        private Language language;

    }

    @Getter
    @Setter
    public static class LoginRequestDTO{
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "패스워드는 필수입니다.")
        private String password;
    }
}
