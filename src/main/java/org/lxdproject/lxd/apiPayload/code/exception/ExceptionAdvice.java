package org.lxdproject.lxd.apiPayload.code.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.dto.ErrorReasonDTO;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.discord.DiscordAlarmAsyncFacade;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
@RequiredArgsConstructor
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    private final DiscordAlarmAsyncFacade discord;

    // 공통 실패 처리 메서드
    private ResponseEntity<Object> handleFailure(Exception e, ErrorStatus status, String message, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(status.getCode(), message, null);
        return super.handleExceptionInternal(e, body, HttpHeaders.EMPTY, status.getHttpStatus(), request);
    }

    // @Valid @RequestBody (DTO) DTO 유효성 검증 실패
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                               HttpHeaders headers,
                                                               HttpStatusCode status,
                                                               WebRequest request) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorStatus.VALIDATOR_ERROR.getMessage());

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorStatus.VALIDATOR_ERROR.getCode(), message, null
        );

        return super.handleExceptionInternal(
                e, body, headers,
                ErrorStatus.VALIDATOR_ERROR.getHttpStatus(), request
        );
    }

    // 커스텀 Validator (예: @MaxImageCount)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e,
                                                                     WebRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse(ErrorStatus.VALIDATOR_ERROR.getMessage());

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorStatus.VALIDATOR_ERROR.getCode(), message, null
        );

        return super.handleExceptionInternal(
                e, body, HttpHeaders.EMPTY,
                ErrorStatus.VALIDATOR_ERROR.getHttpStatus(), request
        );
    }

    // 서비스 계층 GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> handleGeneralException(GeneralException e, HttpServletRequest request) {
        ErrorReasonDTO reason = e.getErrorReasonHttpStatus();
        ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);

        return super.handleExceptionInternal(
                e, body, HttpHeaders.EMPTY,
                reason.getHttpStatus(), new ServletWebRequest(request)
        );
    }

    // 모든 기타 예외 처리 (예상치 못한 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception e, HttpServletRequest req, WebRequest request) {
        log.error("[UNCAUGHT_EXCEPTION]", e);  // 민감 정보는 로그로만

        try {
            discord.sendErrorAlertAsync(
                    e,
                    req.getMethod(),
                    req.getRequestURI(),
                    req.getQueryString() != null ? req.getQueryString() : ""
            );
        } catch (Exception ignore) {
        }

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorStatus._INTERNAL_SERVER_ERROR.getCode(),
                ErrorStatus._INTERNAL_SERVER_ERROR.getMessage(),
                null
        );

        return super.handleExceptionInternal(
                e, body, HttpHeaders.EMPTY,
                ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(), request
        );
    }

}
