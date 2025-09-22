package org.lxdproject.lxd.member;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Style;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private DiaryCommentRepository diaryCommentRepository;
    @Autowired private DiaryLikeRepository diaryLikeRepository;
    @Autowired private DiaryCommentLikeRepository diaryCommentLikeRepository;
    @Autowired private MemberService memberService;

    @Test
    @DisplayName("탈퇴 후 30일 이전의 회원은 soft delete, 일기/댓글은 soft delete, 좋아요는 hard delete 됩니다")
    void deleteMember_shouldSoftDeleteMemberAndContents_andHardDeleteLikes() {
        // [given] Member + Diary + DiaryComment + DiaryLike + DiaryCommentLike
        Member member = Member.builder()
                .username("softUser")
                .password("pw")
                .email("soft@test.com")
                .nickname("소프트")
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
                .title("소프트 일기")
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
                .commentText("소프트 댓글")
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

        // [when] 탈퇴 처리 (deleteMember는 soft + like hard delete)
        memberService.deleteMember(member.getId());

        // [then] member/diary/comment는 soft delete (deletedAt != null)
        Member withdrawn = memberRepository.findById(member.getId()).orElseThrow();
        Diary withdrawnDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        DiaryComment withdrawnComment = diaryCommentRepository.findById(comment.getId()).orElseThrow();

        assertThat(withdrawn.getDeletedAt()).isNotNull();
        assertThat(withdrawnDiary.getDeletedAt()).isNotNull();
        assertThat(withdrawnComment.getDeletedAt()).isNotNull();

        // [then] 좋아요는 hard delete (repo 조회 시 존재하지 않아야 함)
        assertThat(diaryLikeRepository.findById(diaryLike.getId())).isEmpty();
        assertThat(diaryCommentLikeRepository.findById(commentLike.getId())).isEmpty();
    }

    @Test
    @DisplayName("탈퇴 후 30일 지난 회원, 일기, 댓글이 hard delete 됩니다")
    void hardDeleteWithdrawnMembers_shouldRemoveOldData() {
        // [given] Member + Diary + DiaryComment
        Member member = Member.builder()
                .username("testUser")
                .password("encodedPw")
                .email("test@test.com")
                .nickname("테스터")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(false)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(member);

        Diary diary = Diary.builder()
                .member(member)
                .title("테스트 일기")
                .content("내용입니다")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diary);

        DiaryComment comment = DiaryComment.builder()
                .member(member)
                .diary(diary)
                .commentText("댓글입니다")
                .build();
        diaryCommentRepository.save(comment);

        // soft delete 시점을 31일 전 수정
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
        member.softDelete(deletedAt);
        diary.softDelete(deletedAt);
        comment.softDelete(deletedAt);

        // [when] hard delete 실행
        memberService.hardDeleteWithdrawnMembers();

        // [then] 데이터가 완전히 삭제되었는지 검증
        assertThat(memberRepository.findById(member.getId())).isEmpty();
        assertThat(diaryRepository.findById(diary.getId())).isEmpty();
        assertThat(diaryCommentRepository.findById(comment.getId())).isEmpty();
    }

}
