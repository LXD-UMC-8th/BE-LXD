package org.lxdproject.lxd.integration.auth;
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
        memberService.softDeleteMember(member.getId());


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

    @Test
    @DisplayName("회원 복구 시, 해당 회원이 작성한 일기에 달린 댓글/좋아요, 작성한 댓글에 달린 좋아요도 복구된다")
    void recoverMember_shouldRestoreRelatedEntities() {
        // [given] 탈퇴할 회원 A, 연관 회원 B, C
        Member memberA = Member.builder()
                .username("memberA_recover")
                .password("pw")
                .email("a@test.com")
                .nickname("A")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberA);

        Member memberB = Member.builder()
                .username("memberB_recover")
                .password("pw")
                .email("b@test.com")
                .nickname("B")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberB);

        Member memberC = Member.builder()
                .username("memberC_recover")
                .password("pw")
                .email("c@test.com")
                .nickname("C")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberC);

        Diary diaryA = Diary.builder()
                .member(memberA)
                .title("A의 일기 복구")
                .content("A의 내용")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diaryA);

        // B가 A의 일기에 댓글, 좋아요
        DiaryComment commentB_on_A = DiaryComment.builder()
                .member(memberB)
                .diary(diaryA)
                .commentText("B의 댓글")
                .build();
        diaryCommentRepository.save(commentB_on_A);
        DiaryLike likeB_on_A = DiaryLike.builder()
                .member(memberB)
                .diary(diaryA)
                .build();
        diaryLikeRepository.save(likeB_on_A);

        // A가 댓글 작성
        DiaryComment commentA = DiaryComment.builder()
                .member(memberA)
                .diary(diaryA)
                .commentText("A의 댓글")
                .build();
        diaryCommentRepository.save(commentA);

        // C가 A의 댓글에 좋아요
        DiaryCommentLike likeC_on_commentA = DiaryCommentLike.builder()
                .member(memberC)
                .comment(commentA)
                .build();
        diaryCommentLikeRepository.save(likeC_on_commentA);

        // A 탈퇴
        memberService.softDeleteMember(memberA.getId());

        // [when]
        // CustomUserDetails 객체 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(memberA);

        // SecurityContext에 CustomUserDetails 주입
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        authService.recover();

        // then (A의 일기에 달린 댓글/좋아요, A의 댓글에 달린 좋아요도 복구됨)
        DiaryComment restoredCommentB = diaryCommentRepository.findById(commentB_on_A.getId()).orElseThrow();
        DiaryLike restoredLikeB = diaryLikeRepository.findById(likeB_on_A.getId()).orElseThrow();
        DiaryCommentLike restoredLikeC = diaryCommentLikeRepository.findById(likeC_on_commentA.getId()).orElseThrow();

        assertThat(restoredCommentB.getDeletedAt()).isNull();
        assertThat(restoredLikeB.getDeletedAt()).isNull();
        assertThat(restoredLikeC.getDeletedAt()).isNull();

        // test 끝나면 context 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원 B, C가 복구하면 A의 일기 commentCount, likeCount, 댓글의 likeCount도 증가한다")
    void recoverMembers_shouldUpdateDiaryAndCommentCounts() {
        // [given] A(작성자), B(일기 댓글+좋아요), C(댓글 좋아요)
        Member memberA = Member.builder()
                .username("memberA")
                .password("pw")
                .email("a@test.com")
                .nickname("A")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberA);

        Member memberB = Member.builder()
                .username("memberB")
                .password("pw")
                .email("b@test.com")
                .nickname("B")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberB);

        Member memberC = Member.builder()
                .username("memberC")
                .password("pw")
                .email("c@test.com")
                .nickname("C")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(true)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberC);

        // [A]의 일기 생성
        Diary diaryA1 = Diary.builder()
                .member(memberA)
                .title("A의 일기")
                .content("내용")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .commentCount(2)  // A가 작성한 댓글 + B가 작성한 댓글
                .likeCount(1) // B가 누른 좋아요
                .build();
        diaryRepository.save(diaryA1);

        // [B]가 A의 일기에 댓글 + 좋아요
        DiaryComment commentB_on_diaryA1 = DiaryComment.builder()
                .member(memberB)
                .diary(diaryA1)
                .commentText("B의 댓글")
                .build();
        diaryCommentRepository.save(commentB_on_diaryA1);

        DiaryLike likeB_on_diaryA1 = DiaryLike.builder()
                .member(memberB)
                .diary(diaryA1)
                .build();
        diaryLikeRepository.save(likeB_on_diaryA1);

        // [A]가 댓글 작성
        DiaryComment commentA1 = DiaryComment.builder()
                .member(memberA)
                .diary(diaryA1)
                .commentText("A의 댓글")
                .likeCount(1) // C가 누른 댓글 좋아요
                .build();
        diaryCommentRepository.save(commentA1);

        // [C]가 A의 댓글에 좋아요
        DiaryCommentLike likeC_on_commentA1 = DiaryCommentLike.builder()
                .member(memberC)
                .comment(commentA1)
                .build();
        diaryCommentLikeRepository.save(likeC_on_commentA1);


        // B 탈퇴 → A의 일기 관련 데이터 soft delete 및 commentCount 및 likeCount 감소
        memberService.softDeleteMember(memberB.getId());
        // C 탈퇴 → A의 댓글 관련 데이터 soft delete 및 likeCount 감소
        memberService.softDeleteMember(memberC.getId());

        // [when]
        // CustomUserDetails 객체 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(memberB);

        // SecurityContext에 CustomUserDetails 주입
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // B 복구
        authService.recover();

        // context 초기화
        SecurityContextHolder.clearContext();

        // CustomUserDetails 객체 생성
        customUserDetails = new CustomUserDetails(memberC);
        // SecurityContext에 CustomUserDetails 주입
        authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // C 복구
        authService.recover();


        // [then] 일기 상태 재조회
        Diary afterBRecoveryDiary = diaryRepository.findById(diaryA1.getId()).orElseThrow();
        DiaryComment afterBRecoveryComment = diaryCommentRepository.findById(commentA1.getId()).orElseThrow();

        // A의 일기 commentCount / likeCount가 복구됐는지 확인
        assertThat(afterBRecoveryDiary.getCommentCount()).isEqualTo(2);
        assertThat(afterBRecoveryDiary.getLikeCount()).isEqualTo(1);

        // [then] 댓글 상태 재조회
        DiaryComment afterCRecoveryComment = diaryCommentRepository.findById(commentA1.getId()).orElseThrow();
        DiaryCommentLike afterCRecoveryLikeC = diaryCommentLikeRepository.findById(likeC_on_commentA1.getId()).orElseThrow();

        // A의 댓글 likeCount가 복구됐는지 확인
        assertThat(afterCRecoveryComment.getLikeCount()).isEqualTo(1);

    }

}