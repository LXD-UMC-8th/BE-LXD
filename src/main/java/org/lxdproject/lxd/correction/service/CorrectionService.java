package org.lxdproject.lxd.correction.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public CorrectionResponseDTO.CreateResponseDTO createCorrection(
            CorrectionRequestDTO.CreateRequestDTO requestDto,
            Member author
    ) {
        Diary diary = diaryRepository.findById(requestDto.getDiaryId())
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Correction correction = Correction.builder()
                .diary(diary)
                .author(author)
                .originalText(requestDto.getOriginal())
                .corrected(requestDto.getCorrected())
                .commentText(requestDto.getCommentText())
                .likeCount(0)
                .commentCount(0)
                .build();

        Correction saved = correctionRepository.save(correction);

        return CorrectionResponseDTO.CreateResponseDTO.builder()
                .correctionId(saved.getId())
                .diaryId(saved.getDiary().getId())
                .createdAt(saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy. MM. dd a hh:mm:ss").withLocale(Locale.KOREA)))
                .original(saved.getOriginalText())
                .corrected(saved.getCorrected())
                .commentText(saved.getCommentText())
                .likeCount(saved.getLikeCount())
                .commentCount(saved.getCommentCount())
                .isLikedByMe(false)
                .author(CorrectionResponseDTO.AuthorDTO.builder()
                        .memberId(author.getId())
                        .userId(author.getUsername())
                        .nickname(author.getNickname())
                        .profileImageUrl(author.getProfileImg())
                        .build())
                .build();
    }
}