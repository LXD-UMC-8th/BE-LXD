package org.lxdproject.lxd.diary.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.dto.DiaryRequestDTO;
import org.lxdproject.lxd.diary.dto.DiaryResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryResponseDTO createDiary(DiaryRequestDTO request) {
        Member member = null;

//        if (request.getMemberId() != null) {
//            member = memberRepository.findById(request.getMemberId())
//                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
//        }

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
}
