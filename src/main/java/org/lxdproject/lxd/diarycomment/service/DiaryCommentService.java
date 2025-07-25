package org.lxdproject.lxd.diarycomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {

    private final DiaryCommentRepository diaryCommentRepository;

    public DiaryCommentResponseDTO writeComment(Long userId, Long diaryId, DiaryCommentRequestDTO request) {
        DiaryComment comment = DiaryComment.builder()
                .userId(userId)
                .diaryId(diaryId)
                .commentText(request.getCommentText())
                .parentId(request.getParentId())
                .likeCount(0)
                .build();

        DiaryComment saved = diaryCommentRepository.save(comment);

        return DiaryCommentResponseDTO.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .diaryId(saved.getDiaryId())
                .commentText(saved.getCommentText())
                .parentId(saved.getParentId())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}

//handler추가 후 변경하기

