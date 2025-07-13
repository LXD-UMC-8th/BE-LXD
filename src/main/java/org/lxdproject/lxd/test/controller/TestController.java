package org.lxdproject.lxd.test.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.exception.handler.InvalidPageException;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.apiPayload.code.status.SuccessStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트용 API", description = "초기 설정 테스트용 API입니다.")
@RestController
public class TestController {

    // 서버 실행 테스트
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("서버가 실행중입니다.");
    }

    // 성공 응답 테스트
    @GetMapping("/test/success")
    public ApiResponse<String> getSuccess() {
        return ApiResponse.onSuccess(SuccessStatus._OK, "성공 응답 테스트 결과값 입니다.");
    }

    // 예외처리 테스트
    @GetMapping("/test/error")
    public ApiResponse<Void> getError() {
        throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    // 커스텀 예외 처리 및 성공 응답 테스트
    @GetMapping("/test/invalid-page")
    public ApiResponse<String> getInvalidPage(@RequestParam(required = false) Integer page) {
        if (page == null || page < 1) {
            throw new InvalidPageException(ErrorStatus.INVALID_PAGE);
        }
        return ApiResponse.onSuccess(SuccessStatus._OK,   page + "번 페이지를 조회 성공했습니다.");
    }

    // 실패 응답 테스트
    @GetMapping("/test/failure")
    public ApiResponse<String> testFailure() {
        return ApiResponse.onFailure(ErrorStatus.TEST_FAIL, "실패 응답 테스트 결과값 입니다.");
    }
}