package org.lxdproject.lxd.diary.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.util.S3Uploader;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.RelationType;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.member.repository.FriendRepository;
import org.lxdproject.lxd.member.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.fraser.neil.plaintext.diff_match_patch;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Transactional
    public DiaryDetailResponseDTO createDiary(DiaryRequestDTO request) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Diary diary = Diary.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .style(request.getStyle())
                .visibility(request.getVisibility())
                .commentPermission(request.getCommentPermission())
                .language(request.getLanguage())
                .thumbImg(request.getThumbImg())
                .member(member)
                .build();

        diaryRepository.save(diary);

        return DiaryDetailResponseDTO.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponseDTO getDiaryDetail(Long id) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (diary.getVisibility() == Visibility.PRIVATE && !diary.getMember().getId().equals(currentMemberId)) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        return DiaryDetailResponseDTO.from(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (diary.getMember() == null || !diary.getMember().getId().equals(currentMemberId)) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        List<String> urls = extractImageUrls(diary.getContent());
        List<String> keys = s3Uploader.extractS3KeysFromUrls(urls);
        s3Uploader.deleteFiles(keys);

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

    public MyDiarySliceResponseDTO getMyDiaries(int page, int size, Boolean likedOnly) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Pageable pageable = PageRequest.of(page - 1, size);
        return diaryRepository.findMyDiaries(memberId, likedOnly, pageable);
    }

    public DiaryDetailResponseDTO updateDiary(Long id, DiaryRequestDTO request) {

        Long memberId = SecurityUtil.getCurrentMemberId();

        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(memberId)) {
            throw new DiaryHandler(ErrorStatus.FORBIDDEN_DIARY_UPDATE);
        }

        if (diary.getThumbImg() != null && !diary.getThumbImg().equals(request.getThumbImg())) {
            s3Uploader.deleteFileByUrl(diary.getThumbImg());
        }

        String originalContent = diary.getContent(); // 기존 DB에 저장되어있던 일기 content

        // DB에 새로운 내용 저장
        diary.update(request);
        Diary updated = diaryRepository.save(diary);

        // diff 계산
        String diffHtmlContent = generateDiffHtml(originalContent, request.getContent());

        return DiaryDetailResponseDTO.fromWithDiff(updated, diffHtmlContent);
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
        return diaryRepository.getDiaryStatsByMonth(memberId, year, month);
    }

    public DiarySliceResponseDTO getDiariesOfFriends(Long userId, Pageable pageable) {
        return diaryRepository.findDiariesOfFriends(userId, pageable);
    }

    public DiarySliceResponseDTO getLikedDiaries(Pageable pageable) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return diaryRepository.findLikedDiariesOfFriends(currentMemberId, pageable);
    }

    public DiarySliceResponseDTO getExploreDiaries(Pageable pageable, Language language) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return diaryRepository.findExploreDiaries(userId, language, pageable);
    }

    public MemberDiarySummaryResponseDTO getDiarySummary(Long targetMemberId, Long currentMemberId, boolean includeStatus) {
        Member member = memberRepository.findById(targetMemberId)
                               .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Long diaryCount = diaryRepository.countByMemberId(targetMemberId);

        Long friendCount = friendRepository.countFriendsByMemberId(targetMemberId);

        RelationType relation;
        if (targetMemberId.equals(currentMemberId)) {
            relation = RelationType.SELF;
        } else if (friendRepository.existsFriendRelation(targetMemberId, currentMemberId)) {
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
                .profileImg(member.getProfileImg())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .diaryCount(diaryCount)
                .friendCount(friendCount)
                .relation(relation)
                .status(status) // 필드 추가함
                .build();
    }

    public MyDiarySliceResponseDTO getDiariesByMemberId(Long memberId, Pageable pageable) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return diaryRepository.getDiariesByMemberId(userId, memberId, pageable);
    }
}
