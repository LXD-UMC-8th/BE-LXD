package org.lxdproject.lxd.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthRequestDTO {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequestDTO{
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "패스워드는 필수입니다.")
        private String password;
    }

    @Getter
    @Setter
    public static class sendVerificationRequestDTO{
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;
    }

    @Getter
    @Setter
    public static class SocialLoginRequestDTO {
        @NotBlank(message = "소셜 로그인 후 받은 Oauth2 code")
        private String code;
    }

}
