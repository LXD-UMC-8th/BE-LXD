package org.lxdproject.lxd.auth;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.lxdproject.lxd.auth.service.AuthService;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.diary.service.DiaryService;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.diarylike.entity.DiaryLike;
import org.lxdproject.lxd.diarylike.repository.DiaryLikeRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.entity.enums.Status;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.member.service.MemberService;
import org.lxdproject.lxd.schedular.MemberCleanupSchedular;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Transactional
public class AuthServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private DiaryCommentRepository diaryCommentRepository;
    @Autowired private DiaryLikeRepository diaryLikeRepository;
    @Autowired private DiaryCommentLikeRepository diaryCommentLikeRepository;
    @Autowired private MemberService memberService;
    @Autowired private AuthService authService;

    @Test
    @DisplayName("회원 복구 시, member가 작성한 일기/댓글/일기좋아요/댓글좋아요가 모두 복구된다")
    void recoverMember_shouldRestoreAllOwnedEntities() {
        // [given] member가 일기, 댓글, 일기 좋아요, 일기 댓글 좋아요 작성
        Member member = Member.builder()
                .username("restoreUser")
                .password("pw")
                .email("restore@test.com")
                .nickname("jun")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(member);

        Diary diary = Diary.builder()
                .member(member)
                .title("복구 일기")
                .content("내용")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diary);

        DiaryComment comment = DiaryComment.builder()
                .member(member)
                .diary(diary)
                .commentText("복구 댓글")
                .build();
        diaryCommentRepository.save(comment);

        DiaryLike diaryLike = DiaryLike.builder()
                .member(member)
                .diary(diary)
                .build();
        diaryLikeRepository.save(diaryLike);

        DiaryCommentLike commentLike = DiaryCommentLike.builder()
                .member(member)
                .comment(comment)
                .build();
        diaryCommentLikeRepository.save(commentLike);

        // soft delete (회원 탈퇴)
        memberService.deleteMember(member.getId());


        // [when] 회원 복구
        // CustomUserDetails 객체 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        // SecurityContext에 CustomUserDetails 주입
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        authService.recover();


        // [then]
        Member restoredMember = memberRepository.findById(member.getId()).orElseThrow();
        Diary restoredDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        DiaryComment restoredComment = diaryCommentRepository.findById(comment.getId()).orElseThrow();
        DiaryLike restoredDiaryLike = diaryLikeRepository.findById(diaryLike.getId()).orElseThrow();
        DiaryCommentLike restoredCommentLike = diaryCommentLikeRepository.findById(commentLike.getId()).orElseThrow();

        assertThat(restoredMember.getDeletedAt()).isNull();
        assertThat(restoredDiary.getDeletedAt()).isNull();
        assertThat(restoredComment.getDeletedAt()).isNull();
        assertThat(restoredDiaryLike.getDeletedAt()).isNull();
        assertThat(restoredCommentLike.getDeletedAt()).isNull();

        // test 끝나면 context 초기화
        SecurityContextHolder.clearContext();
    }

}