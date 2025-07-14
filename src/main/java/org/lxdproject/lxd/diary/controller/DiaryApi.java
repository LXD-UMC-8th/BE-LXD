package org.lxdproject.lxd.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.diary.dto.DiaryDetailResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.DiaryResponseDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary API", description = "일기 관련 API 입니다.")
@RequestMapping("/diaries")
public interface DiaryApi {

    @PostMapping
    @Operation(summary = "일기 작성 API", description = "이미지는 s3업로드 api로 요청 후 전달된 url을 html content 안에 포함시켜서 저장 요청해주세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200",description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "MEMBER4001", description = "회원 정보가 이상해요!",content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public ApiResponse<DiaryResponseDTO> createDiary(@RequestBody DiaryRequestDTO request);

    @GetMapping("/{id}")
    @Operation(summary = "일기 상세 조회 API", description = "id에 해당하는 일기를 상세 조회하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200",description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "MEMBER4001", description = "회원 정보가 이상해요!",content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "DIARY4001", description = "없는 일기에요",content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "id", description = "일기의 아이디, path variable 입니다!"),
    })
    public ApiResponse<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable Long id);
}
