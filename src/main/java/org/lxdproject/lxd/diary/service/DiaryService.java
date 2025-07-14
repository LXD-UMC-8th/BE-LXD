package org.lxdproject.lxd.diary.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.diary.dto.DiaryDetailResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.DiaryResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryResponseDTO createDiary(DiaryRequestDTO request) {

        // Todo: 현재 로그인한 사용자의 고유번호 넣기
        Member member = null;

        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        }

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

        return new DiaryResponseDTO(
                diary.getId(),
                diary.getThumbImg()
        );
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponseDTO getDiaryDetail(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Member member = diary.getMember();
        if (member == null) {
            throw new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND);
        }

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getVisibility(),
                diary.getTitle(),
                diary.getLanguage(),
                member.getProfileImg(),
                member.getNickname(),
                member.getUsername(),
                diary.getCreatedAt(),
                diary.getCommentCount(),
                diary.getLikeCount(),
                diary.getCorrectionCount(),
                diary.getContent()
        );
    }

}
