package org.lxdproject.lxd.diarycomment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.service.DiaryCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary Comment", description = "일기 댓글 관련 API")
@RestController
@RequestMapping("/diaries/{diaryId}/comments")
@RequiredArgsConstructor
public class DiaryCommentController {

    private final DiaryCommentService diaryCommentService;

    @Operation(
            summary = "댓글 작성",
            description = "특정 일기에 댓글을 작성합니다.",
            security = @SecurityRequirement(name = "BearerAuth") // Swagger용 인증 명시
    )
    @PostMapping
    public ResponseEntity<DiaryCommentResponseDTO> writeComment(
            @PathVariable Long diaryId,
            @RequestBody DiaryCommentRequestDTO request,
            @AuthenticationPrincipal(expression = "username") String userIdStr // userId 추출
    ) {
        Long userId = Long.parseLong(userIdStr);
        DiaryCommentResponseDTO response = diaryCommentService.writeComment(userId, diaryId, request);
        return ResponseEntity.ok(response);
    }
}



