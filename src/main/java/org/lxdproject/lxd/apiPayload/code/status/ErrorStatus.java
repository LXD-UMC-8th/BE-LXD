package org.lxdproject.lxd.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.apiPayload.code.dto.ErrorReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 멤버 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4300", "사용자가 없습니다"),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "MEMBER4311", "이미 존재하는 이메일입니다."),
    NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "MEMBER4312", "이미 존재하는 닉네임입니다."),
    PRIVACY_POLICY_NOT_AGREED(HttpStatus.BAD_REQUEST, "MEMBER4313", "개인정보 동의는 필수입니다."),

    // 인증 관련 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4300", "토큰이 올바르지 않습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH4310", "아이디 또는 비밀번호가 올바르지 않습니다."),
    AUTHENTICATION_INFO_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4320", "인증 정보를 찾을 수 없습니다."),
    INVALID_AUTHENTICATION_INFO(HttpStatus.FORBIDDEN, "AUTH4321", "인증된 사용자 정보가 올바르지 않습니다."),
    NOT_RESOURCE_OWNER(HttpStatus.FORBIDDEN,"AUTH4001","해당 리소스의 작성자가 아닙니다. 권한이 없습니다."),

    // 일기 관련 에러
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND,"DIARY4001","일기를 찾을 수 없습니다."),

    // 메일 관련 에러
    UNABLE_TO_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL 4301", "이메일을 보낼 수 없습니다."),

    // 알림 관련 에러
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND,"NOTIFICATION4001","알림을 찾을 수 없습니다."),

    // 테스트 용 응답
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "PAGE400", "유효하지 않은 페이지 번호입니다."),
    TEST_FAIL(HttpStatus.BAD_REQUEST, "TEST400", "사용자 정의 실패 응답입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}