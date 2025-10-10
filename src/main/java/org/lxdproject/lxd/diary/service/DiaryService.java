package org.lxdproject.lxd.diary.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.guard.PermissionGuard;
import org.lxdproject.lxd.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.diary.util.DiaryUtil;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.lxdproject.lxd.infra.storage.S3FileService;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.RelationType;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.friend.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.lxdproject.lxd.friend.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.text.StringEscapeUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.fraser.neil.plaintext.diff_match_patch;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final MemberRepository memberRepository;
    private final S3FileService s3FileService;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final PermissionGuard permissionGuard;

    @Transactional
    public DiaryDetailResponseDTO createDiary(DiaryRequestDTO request) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = Diary.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .modifiedContent(request.getContent())
                .diffContent(request.getContent())
                .previewContent(DiaryUtil.generateContentPreview(request.getContent()))
                .style(request.getStyle())
                .visibility(request.getVisibility())
                .commentPermission(request.getCommentPermission())
                .language(request.getLanguage())
                .thumbImg(request.getThumbImg())
                .member(member)
                .build();

        diaryRepository.save(diary);

        return DiaryDetailResponseDTO.from(diary, false);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponseDTO getDiaryDetail(Long id) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        permissionGuard.canViewDiary(currentMemberId, diary);

        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(currentMemberId);

        return DiaryDetailResponseDTO.from(diary, likedSet.contains(diary.getId()));
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (diary.getMember() == null || !diary.getMember().getId().equals(currentMemberId)) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        List<String> urls = extractImageUrls(diary.getModifiedContent());
        List<String> keys = s3FileService.extractS3KeysFromUrls(urls);
        s3FileService.deleteImages(keys);

        diary.softDelete();
    }

    private static final Pattern IMG_URL_PATTERN = Pattern.compile("<img[^>]+src=[\"']?([^\"'>]+)[\"']?");

    private List<String> extractImageUrls(String htmlContent) {
        List<String> imageUrls = new ArrayList<>();
        Matcher matcher = IMG_URL_PATTERN.matcher(htmlContent);
        while (matcher.find()) {
            imageUrls.add(matcher.group(1)); // src 값만 추출
        }
        return imageUrls;
    }

    public PageDTO<MyDiarySummaryResponseDTO> getMyDiaries(Boolean likedOnly, int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 좋아요 누른 일기 ID
        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(memberId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findMyDiaries(memberId, likedOnly, pageable);

        List<MyDiarySummaryResponseDTO> dtoList = diaryPage.getContent().stream()
                .map(d -> MyDiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(d.getPreviewContent())
                        .language(d.getLanguage())
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return new PageDTO<>(
                null,
                dtoList,
                page + 1,
                size,
                diaryPage.hasNext()
        );
    }

    public DiaryDetailResponseDTO updateDiary(Long id, DiaryUpdateRequestDTO request) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        if (diary.getMember() == null || !diary.getMember().getId().equals(memberId)) {
            throw new DiaryHandler(ErrorStatus.FORBIDDEN_DIARY_UPDATE);
        }

        if (diary.getThumbImg() != null && !diary.getThumbImg().equals(request.getThumbImg())) {
            s3FileService.deleteImage(diary.getThumbImg());
        }

        // 최초 작성 원문 내용
        String originalContent = diary.getContent();

        // diff 포함된 최종 본문
        String diffContent = generateDiffHtml(originalContent, request.getContent());

        // diff 없는 최종 본문으로 수정 내용 추출
        String previewContent = DiaryUtil.generateContentPreview(request.getContent());

        // DB에 새로운 내용 저장
        diary.update(request, diffContent, request.getContent(), previewContent);
        diaryRepository.save(diary);

        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(memberId);

        return DiaryDetailResponseDTO.from(diary, likedSet.contains(diary.getId()));
    }

    private String generateDiffHtml(String oldContent, String newContent) {
        diff_match_patch dmp = new diff_match_patch();

        // 라이브러리 활용해서 content 간의 diff 계산
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(oldContent, newContent);
        dmp.diff_cleanupSemantic(diffs);

        // 라이브러리에서 삽입하는 스타일 태그 제거
        StringBuilder html = new StringBuilder();
        for (diff_match_patch.Diff diff : diffs) {
            String text = StringEscapeUtils.escapeHtml4(diff.text);

            switch (diff.operation) {
                case INSERT:
                    html.append("<ins>").append(text).append("</ins>");
                    break;
                case DELETE:
                    html.append("<del>").append(text).append("</del>");
                    break;
                case EQUAL:
                    html.append(text);
                    break;
            }
        }
        return html.toString();
    }

    public List<DiaryStatsResponseDTO> getDiaryStats(int year, int month) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return diaryRepository.findDiaryStatsByMonth(memberId, year, month);
    }

    public PageDTO<DiarySummaryResponseDTO> getFriendDiaries(int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(memberId);
        Set<Long> friendIds = friendRepository.findFriendIdsByMemberId(memberId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findFriendDiaries(memberId, friendIds, pageable);

        List<DiarySummaryResponseDTO> dtoList = diaryPage.getContent().stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(d.getPreviewContent())
                        .language(d.getLanguage())
                        .writerMemberProfile(MemberProfileDTO.from(d.getMember()))
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return new PageDTO<>(
                null,
                dtoList,
                page + 1,
                size,
                diaryPage.hasNext()
        );
    }

    public PageDTO<DiarySummaryResponseDTO> getLikedDiaries(int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 좋아요 누른 일기 ID
        List<Long> likedDiaryIds = diaryLikeRepository.findLikedDiaryIdList(memberId);
        if (likedDiaryIds.isEmpty()) {
            return new PageDTO<>(null, List.of(), page + 1, size, false);
        }

        // 성능 개선
        Set<Long> likedSet = new HashSet<>(likedDiaryIds);
        Set<Long> friendIds = friendRepository.findFriendIdsByMemberId(memberId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findLikedDiaries(memberId, likedDiaryIds, friendIds, pageable);

        List<DiarySummaryResponseDTO> dtoList = diaryPage.getContent().stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(d.getPreviewContent())
                        .language(d.getLanguage())
                        .writerMemberProfile(MemberProfileDTO.from(d.getMember()))
                        .liked(likedSet.contains(d.getId()))
                        .build()
                )
                .toList();

        return new PageDTO<>(
                null,
                dtoList,
                page + 1,
                size,
                diaryPage.hasNext()
        );
    }

    public PageDTO<DiarySummaryResponseDTO> getExploreDiaries(int page, int size, Language language) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(memberId);
        Set<Long> friendIds = friendRepository.findFriendIdsByMemberId(memberId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findExploreDiaries(memberId, language, friendIds, pageable);

        List<DiarySummaryResponseDTO> dto = diaryPage.getContent().stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .writerMemberProfile(MemberProfileDTO.from(d.getMember()))
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(d.getPreviewContent())
                        .language(d.getLanguage())
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return new PageDTO<>(
                null,
                dto,
                page + 1,
                size,
                diaryPage.hasNext()
        );

    }

    public MemberDiarySummaryResponseDTO getDiarySummary(Long targetMemberId, Long currentMemberId, boolean includeStatus) {
        Member member = memberRepository.findById(targetMemberId)
                               .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

        Long diaryCount = diaryRepository.countByMemberIdAndDeletedAtIsNull(targetMemberId);

        Long friendCount = friendRepository.countFriendsByMemberId(targetMemberId);

        RelationType relation;
        if (targetMemberId.equals(currentMemberId)) {
            relation = RelationType.SELF;
        } else if (friendRepository.areFriends(targetMemberId, currentMemberId)) {
            relation = RelationType.FRIEND;
        } else {
            relation = RelationType.NONE;
        }

        // 추가된 FriendRequestStatus 조회 로직
        FriendRequestStatus status = null;

        if (includeStatus && !targetMemberId.equals(currentMemberId)) {
            status = friendRequestRepository.findByRequesterIdAndReceiverId(currentMemberId, targetMemberId)
                    .map(FriendRequest::getStatus)
                    .orElse(null);
        }

        return MemberDiarySummaryResponseDTO.builder()
                .memberProfile(MemberProfileDTO.from(member))
                .nativeLanguage(member.getNativeLanguage())
                .language(member.getLanguage())
                .diaryCount(diaryCount)
                .friendCount(friendCount)
                .relation(relation)
                .status(status) // 필드 추가함
                .build();
    }

    public PageDTO<MyDiarySummaryResponseDTO> getDiariesByMemberId(Long memberId, int page, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }

        Long viewerId = SecurityUtil.getCurrentMemberId();
        Set<Long> likedSet = diaryLikeRepository.findLikedDiaryIdSet(viewerId);
        Set<Long> friendIds = friendRepository.findFriendIdsByMemberId(viewerId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findDiariesByMemberId(viewerId, memberId, friendIds, pageable);

        List<MyDiarySummaryResponseDTO> dto = diaryPage.getContent().stream()
                .map(d -> MyDiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(d.getPreviewContent())
                        .language(d.getLanguage())
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return new PageDTO<>(
                null,
                dto,
                page + 1,
                size,
                diaryPage.hasNext()
        );
    }

}
