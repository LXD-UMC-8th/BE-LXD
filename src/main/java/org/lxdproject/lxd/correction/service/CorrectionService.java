package org.lxdproject.lxd.correction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.dto.CorrectionRequestDTO;
import org.lxdproject.lxd.correction.dto.CorrectionResponseDTO;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
                .createdAt(formatDate(saved.getCreatedAt()))
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

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.ProvidedCorrectionsResponseDTO getMyProvidedCorrections(Member member, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Correction> corrections = correctionRepository.findByAuthor(member, pageable);

        List<CorrectionResponseDTO.CorrectionItem> correctionItems = corrections.getContent().stream()
                .map(correction -> CorrectionResponseDTO.CorrectionItem.builder()
                        .correctionId(correction.getId())
                        .diaryId(correction.getDiary().getId())
                        .diaryTitle(correction.getDiary().getTitle())
                        .diaryCreatedAt(formatDate(correction.getDiary().getCreatedAt()))
                        .createdAt(formatDate(correction.getCreatedAt()))
                        .original(correction.getOriginalText())
                        .corrected(correction.getCorrected())
                        .commentText(correction.getCommentText())
                        .build())
                .toList();

        return CorrectionResponseDTO.ProvidedCorrectionsResponseDTO.builder()
                .member(CorrectionResponseDTO.AuthorDTO.builder()
                        .memberId(member.getId())
                        .userId(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImg())
                        .build())
                .corrections(correctionItems)
                .page(page)
                .size(size)
                .totalCount((int) corrections.getTotalElements())
                .hasNext(corrections.hasNext())
                .build();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy. MM. dd a hh:mm", Locale.KOREA));
    }
}