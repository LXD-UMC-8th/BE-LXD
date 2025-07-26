package org.lxdproject.lxd.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lxdproject.lxd.apiPayload.ApiResponse;
import org.lxdproject.lxd.common.dto.ImageResponseDTO;
import org.lxdproject.lxd.diary.dto.*;
import org.springframework.http.MediaType;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Diary API", description = "일기 관련 API 입니다.")
@RequestMapping("/diaries")
public interface DiaryApi {

    @PostMapping
    @Operation(summary = "일기 작성 API", description = "이미지는 s3업로드 api로 요청 후 전달된 url을 html content 안에 포함시켜서 저장 요청해주세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "일기 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (JWT 누락 또는 만료)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public ApiResponse<DiaryDetailResponseDTO> createDiary( @Valid @RequestBody DiaryRequestDTO request);

    @GetMapping("/{diaryId}")
    @Operation(summary = "일기 상세 조회 API", description = "id에 해당하는 일기를 상세 조회하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "일기 상세조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요 (JWT 누락 또는 만료)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 리소스입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    @Parameters({
            @Parameter(name = "diaryId", description = "일기의 아이디, path variable 입니다!"),
    })
    public ApiResponse<DiaryDetailResponseDTO> getDiaryDetail(@PathVariable Long diaryId);

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "일기 삭제 API", description = "id에 해당하는 일기를 삭제합니다. 해당하는 이미지 또한 S3 버킷에서 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "일기 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 리소스입니다.",content = @Content(schema = @Schema(implementation = ApiResponse.class))),
    })
    public ApiResponse<Boolean> deleteDiary(@PathVariable Long diaryId);

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "일기 이미지 업로드 API", description = "이미지를 업로드하면 S3의 diary 폴더에 저장되고 해당 URL을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미지 파일이 유효하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<ImageResponseDTO> uploadDiaryImage(
            @Parameter(
                    description = "업로드할 이미지 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("image") MultipartFile image
    );

    @GetMapping("/random-question")
    @Operation(summary = "일기 작성 시 랜덤 질문 조회 API", description = "쿼리 파라미터로 전달된 언어(Language)에 따라 질문 중 하나를 랜덤하게 반환합니다.")
    @Parameters({
            @Parameter(name = "language", description = "KO 또는 ENG 중 원하는 랜덤 질문의 언어를 선택합니다.", required = true)
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "랜덤 질문 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<QuestionResponseDTO> getRandomQuestion(@RequestParam("language") Language language);

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 일기 목록 조회 API", description = "현재 로그인한 사용자가 작성한 일기 목록을 조회합니다. likedOnly=true 시 좋아요 누른 내 일기만 조회됩니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1"),
            @Parameter(name = "size", description = "페이지 크기", example = "10"),
            @Parameter(name = "likedOnly", description = "true일 경우 내가 좋아요 누른 내 일기만 필터링")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일기 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<DiarySliceResponseDTO> getMyDiaries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean likedOnly
    );

    @PatchMapping("/{diaryId}")
    @Operation(summary = "일기 수정 API", description = "title, content, style, visibility, commentPermission, language, thumbImg 필드를 수정합니다.")
    @Parameters({
            @Parameter(name = "diaryId", description = "수정할 일기의 ID", required = true)
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일기 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 리소스입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<DiaryDetailResponseDTO> updateDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryRequestDTO request
    );



    @GetMapping("/stats")
    @Operation(summary = "날짜별 일기 작성 개수 조회 API", description = "로그인한 사용자의 월별 일기 작성 개수를 날짜별로 조회합니다.")
    @Parameters({
            @Parameter(name = "year", description = "연도 (예: 2025)", example = "2025"),
            @Parameter(name = "month", description = "월 (1~12)", example = "7")
    })
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일기 통계 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ApiResponse<List<DiaryStatsResponseDTO>> getDiaryStats(
            @RequestParam int year,
            @RequestParam int month
    );

}
