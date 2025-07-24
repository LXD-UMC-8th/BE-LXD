package org.lxdproject.lxd.correction.service;

import org.lxdproject.lxd.correction.repository.MemberSavedCorrectionRepository;
import org.springframework.data.domain.*;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final CorrectionRepository correctionRepository;
    private final DiaryRepository diaryRepository;
    private final MemberSavedCorrectionRepository memberSavedCorrectionRepository;

    @Transactional(readOnly = true)
    public CorrectionResponseDTO.DiaryCorrectionsResponseDTO getCorrectionsByDiaryId(
            Long diaryId, int page, int size, Member currentMember) {

        if (!diaryRepository.existsById(diaryId)) {
            throw new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Correction> correctionSlice = correctionRepository.findByDiaryId(diaryId, pageable);

        if (correctionSlice.isEmpty()) {
            return emptyDiaryCorrectionsResponse(diaryId);
        }

        Set<Long> likedIds = findLikedCorrectionIds(currentMember, correctionSlice.getContent());

        List<CorrectionResponseDTO.CorrectionDetailDTO> correctionDetailList = correctionSlice.stream()
                .map(correction -> CorrectionResponseDTO.CorrectionDetailDTO.builder()
                        .correctionId(correction.getId())
                        .diaryId(correction.getDiary().getId())
                        .createdAt(formatDate(correction.getCreatedAt()))
                        .original(correction.getOriginalText())
                        .corrected(correction.getCorrected())
                        .commentText(correction.getCommentText())
                        .likeCount(correction.getLikeCount())
                        .commentCount(correction.getCommentCount())
                        .isLikedByMe(likedIds.contains(correction.getId()))
                        .member(CorrectionResponseDTO.MemberDTO.builder()
                                .memberId(correction.getAuthor().getId())
                                .userId(correction.getAuthor().getUsername())
                                .nickname(correction.getAuthor().getNickname())
                                .profileImageUrl(correction.getAuthor().getProfileImg())
                                .build())
                        .build())
                .toList();

        return CorrectionResponseDTO.DiaryCorrectionsResponseDTO.builder()
                .diaryId(diaryId)
                .totalCount(correctionDetailList.size())
                .hasNext(correctionSlice.hasNext())
                .corrections(correctionDetailList)
                .build();
    }

    @Transactional
    public CorrectionResponseDTO.CorrectionDetailDTO createCorrection(
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

        return CorrectionResponseDTO.CorrectionDetailDTO.builder()
                .correctionId(saved.getId())
                .diaryId(saved.getDiary().getId())
                .createdAt(formatDate(saved.getCreatedAt()))
                .original(saved.getOriginalText())
                .corrected(saved.getCorrected())
                .commentText(saved.getCommentText())
                .likeCount(saved.getLikeCount())
                .commentCount(saved.getCommentCount())
                .isLikedByMe(false)
                .member(CorrectionResponseDTO.MemberDTO.builder()
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

        Slice<Correction> corrections = correctionRepository.findByAuthor(member, pageable);

        List<CorrectionResponseDTO.SavedCorrectionItem> correctionItems = corrections.getContent().stream()
                .map(correction -> CorrectionResponseDTO.SavedCorrectionItem.builder()
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
                .member(CorrectionResponseDTO.MemberDTO.builder()
                        .memberId(member.getId())
                        .userId(member.getUsername())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImg())
                        .build())
                .corrections(correctionItems)
                .page(page)
                .size(size)
                .hasNext(corrections.hasNext())
                .build();
    }

    private CorrectionResponseDTO.DiaryCorrectionsResponseDTO emptyDiaryCorrectionsResponse(Long diaryId) {
        return CorrectionResponseDTO.DiaryCorrectionsResponseDTO.builder()
                .diaryId(diaryId)
                .totalCount(0)
                .hasNext(false)
                .corrections(Collections.emptyList())
                .build();
    }

    private Set<Long> findLikedCorrectionIds(Member member, List<Correction> corrections) {
        List<Long> correctionIds = corrections.stream()
                .map(Correction::getId)
                .toList();

        return new HashSet<>(memberSavedCorrectionRepository
                .findLikedCorrectionIdsByMember(member, correctionIds));
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy. MM. dd a hh:mm", Locale.KOREA));
    }
}