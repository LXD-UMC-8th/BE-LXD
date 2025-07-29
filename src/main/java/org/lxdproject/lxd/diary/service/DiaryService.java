package org.lxdproject.lxd.diary.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.util.S3Uploader;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.dto.DiaryDetailResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.MyDiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

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

        // 비공개 일기의 경우 작성자만 접근 가능
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

        diary.update(request);
        Diary updated = diaryRepository.save(diary);
        return DiaryDetailResponseDTO.from(updated);
    }

    public List<DiaryStatsResponseDTO> getDiaryStats(int year, int month) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return diaryRepository.getDiaryStatsByMonth(memberId, year, month);
    }

    public MyDiarySliceResponseDTO getDiariesOfFriends(Long userId, Pageable pageable) {
        return diaryRepository.findDiariesOfFriends(userId, pageable);
    }
}
