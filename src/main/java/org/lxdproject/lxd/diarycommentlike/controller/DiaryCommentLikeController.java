package org.lxdproject.lxd.diarycommentlike.controller;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.diarycommentlike.service.DiaryCommentLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries/{diaryId}/comments/{commentId}/likes")
public class DiaryCommentLikeController {

    private final DiaryCommentLikeService diaryCommentLikeService;

    @PostMapping
    public ResponseEntity<DiaryCommentLikeResponseDTO> toggleLike(
            @PathVariable Long diaryId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "username") String memberIdStr
    ) {
        Long memberId = Long.parseLong(memberIdStr);
        return ResponseEntity.ok(diaryCommentLikeService.toggleLike(memberId, diaryId, commentId));
    }
}
