package org.lxdproject.lxd.integration.member;

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
import org.lxdproject.lxd.friend.entity.FriendRequest;
import org.lxdproject.lxd.friend.entity.Friendship;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.lxdproject.lxd.friend.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.entity.enums.Status;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.member.service.MemberService;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.lxdproject.lxd.schedular.MemberCleanupSchedular;
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
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private FriendRepository friendRepository;
    @Autowired private FriendRequestRepository friendRequestRepository;
    @Autowired private MemberService memberService;

    @Test
    @DisplayName("회원 탈퇴 시 사용자, 사용자가 작성한 일기/댓글, 사용자가 누른 일기 좋아요/댓글 좋아요 모두 soft delete 됩니다")
    void deleteMember_shouldSoftDeleteAllEntities() {
        // given
        Member member = Member.builder()
                .username("softuser")
                .password("pw")
                .email("jun@test.com")
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

        // 일기
        Diary diary = Diary.builder()
                .member(member)
                .title("일기 제목")
                .content("내용")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diary);

        // 댓글
        DiaryComment comment = DiaryComment.builder()
                .member(member)
                .diary(diary)
                .commentText("댓글입니다")
                .build();
        diaryCommentRepository.save(comment);

        // 일기 좋아요
        DiaryLike diaryLike = DiaryLike.builder()
                .member(member)
                .diary(diary)
                .build();
        diaryLikeRepository.save(diaryLike);

        // 댓글 좋아요
        DiaryCommentLike commentLike = DiaryCommentLike.builder()
                .member(member)
                .comment(comment)
                .build();
        diaryCommentLikeRepository.save(commentLike);

        // when
        memberService.softDeleteMember(member.getId());

        // then
        Member deletedMember = memberRepository.findById(member.getId()).orElseThrow();
        Diary deletedDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        DiaryComment deletedComment = diaryCommentRepository.findById(comment.getId()).orElseThrow();
        DiaryLike deletedDiaryLike = diaryLikeRepository.findById(diaryLike.getId()).orElseThrow();
        DiaryCommentLike deletedCommentLike = diaryCommentLikeRepository.findById(commentLike.getId()).orElseThrow();

        // soft delete 확인
        assertThat(deletedMember.getDeletedAt()).isNotNull();
        assertThat(deletedDiary.getDeletedAt()).isNotNull();
        assertThat(deletedComment.getDeletedAt()).isNotNull();
        assertThat(deletedDiaryLike.getDeletedAt()).isNotNull();
        assertThat(deletedCommentLike.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 탈퇴 시, 탈퇴한 회원이 작성한 일기의 일기 좋아요 및 댓글과 탈퇴한 회원이 작성한 댓글의 댓글 좋아요가 soft delete 된다")
    void deleteMember_shouldAlsoSoftDeleteRelatedEntities() {
        // [given] 탈퇴할 회원 A, 다른 회원 B, C
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

        // [A]가 작성한 일기
        Diary diaryA1 = Diary.builder()
                .member(memberA)
                .title("A의 일기")
                .content("A가 쓴 내용")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diaryA1);

        // [B]가 A의 일기에 단 댓글
        DiaryComment commentB_on_diaryA1 = DiaryComment.builder()
                .member(memberB)
                .diary(diaryA1)
                .commentText("B가 쓴 댓글")
                .build();
        diaryCommentRepository.save(commentB_on_diaryA1);

        // [B]가 A의 일기에 누른 좋아요
        DiaryLike diaryLikeB_on_diaryA1 = DiaryLike.builder()
                .member(memberB)
                .diary(diaryA1)
                .build();
        diaryLikeRepository.save(diaryLikeB_on_diaryA1);

        // [A]가 작성한 댓글
        DiaryComment commentA1 = DiaryComment.builder()
                .member(memberA)
                .diary(diaryA1)
                .commentText("A의 댓글")
                .build();
        diaryCommentRepository.save(commentA1);

        // [C]가 A의 댓글에 누른 좋아요
        DiaryCommentLike commentLikeC_on_commentA1 = DiaryCommentLike.builder()
                .member(memberC)
                .comment(commentA1)
                .build();
        diaryCommentLikeRepository.save(commentLikeC_on_commentA1);


        // [when] 탈퇴 실행
        memberService.softDeleteMember(memberA.getId());

        // [then] A의 리소스와 연관된 리소스들 soft delete 확인
        DiaryComment deletedCommentB = diaryCommentRepository.findById(commentB_on_diaryA1.getId()).orElseThrow();
        DiaryLike deletedDiaryLikeB = diaryLikeRepository.findById(diaryLikeB_on_diaryA1.getId()).orElseThrow();
        DiaryCommentLike deletedCommentLikeC = diaryCommentLikeRepository.findById(commentLikeC_on_commentA1.getId()).orElseThrow();

        // A의 일기에 달린 다른 사람 댓글 soft delete 확인
        assertThat(deletedCommentB.getDeletedAt()).isNotNull();

        // A의 일기에 달린 다른 사람의 좋아요 soft delete 확인
        assertThat(deletedDiaryLikeB.getDeletedAt()).isNotNull();

        // A가 쓴 댓글에 달린 다른 사람의 좋아요 soft delete 확인
        assertThat(deletedCommentLikeC.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 B, C가 탈퇴하면 A의 일기 commentCount, likeCount, 댓글의 likeCount도 감소한다")
    void deleteMembers_shouldUpdateDiaryAndCommentCounts() {
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




        // [when] B 탈퇴 → A의 일기 관련 데이터 soft delete 및 commentCount 및 likeCount 감소
        memberService.softDeleteMember(memberB.getId());

        // [then] 일기 상태 재조회
        Diary afterBDeletedDiary = diaryRepository.findById(diaryA1.getId()).orElseThrow();
        DiaryComment afterBDeletedComment = diaryCommentRepository.findById(commentA1.getId()).orElseThrow();

        // A의 일기 commentCount / likeCount가 감소했는지 확인
        assertThat(afterBDeletedDiary.getCommentCount()).isEqualTo(1);
        assertThat(afterBDeletedDiary.getLikeCount()).isEqualTo(0);





        // [when] C 탈퇴 → A의 댓글 관련 데이터 soft delete 및 likeCount 감소
        memberService.softDeleteMember(memberC.getId());

        // [then] A의 댓글 재조회
        DiaryComment afterCDeletedComment = diaryCommentRepository.findById(commentA1.getId()).orElseThrow();
        DiaryCommentLike deletedLikeC = diaryCommentLikeRepository.findById(likeC_on_commentA1.getId()).orElseThrow();

        // A의 댓글 likeCount가 감소했는지 확인
        assertThat(afterCDeletedComment.getLikeCount()).isEqualTo(0);
    }


    @Test
    @DisplayName("탈퇴 후 30일 지난 회원, 일기댓글, 일기댓글좋아요, 일기좋아요, 친구, 친구요청, 알림이 hard delete 됩니다")
    void hardDeleteWithdrawnMembers_shouldRemoveOldData() {
        // [given] Member + Diary + DiaryComment
        Member memberA = Member.builder()
                .username("test_user")
                .password("encodedPw")
                .email("test@test.com")
                .nickname("A")
                .role(Role.USER)
                .loginType(LoginType.LOCAL)
                .nativeLanguage(Language.KO)
                .language(Language.ENG)
                .systemLanguage(Language.KO)
                .isPrivacyAgreed(true)
                .isAlarmAgreed(false)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(memberA);

        Member memberB = Member.builder()
                .username("test_friend")
                .password("encodedPw")
                .email("friend@test.com")
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
                .username("test_friendRequest")
                .password("encodedPw")
                .email("friendRequest@test.com")
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

        Diary diary = Diary.builder()
                .member(memberA)
                .title("A 일기")
                .content("내용입니다")
                .style(Style.FREE)
                .visibility(Visibility.PUBLIC)
                .commentPermission(CommentPermission.ALL)
                .language(Language.KO)
                .build();
        diaryRepository.save(diary);

        DiaryComment comment = DiaryComment.builder()
                .member(memberA)
                .diary(diary)
                .commentText("댓글입니다")
                .build();
        diaryCommentRepository.save(comment);

        DiaryLike diaryLike = DiaryLike.builder()
                .member(memberA)
                .diary(diary)
                .build();
        diaryLikeRepository.save(diaryLike);

        DiaryCommentLike diaryCommentLike = DiaryCommentLike.builder()
                .member(memberA)
                .comment(comment)
                .build();
        diaryCommentLikeRepository.save(diaryCommentLike);

        Notification notification = notificationRepository.save(Notification.builder()
                .sender(memberA)
                .receiver(memberB)
                .notificationType(NotificationType.FRIEND_ACCEPTED)
                .targetType(TargetType.MEMBER)
                .targetId(memberB.getId())
                .redirectUrl("test/" + memberB.getId())
                .build());
        notificationRepository.save(notification);

        Friendship friendship = friendRepository.save(Friendship.builder()
                .requester(memberA)
                .receiver(memberB)
                .build());
        friendRepository.save(friendship);

        FriendRequest friendRequest = friendRequestRepository.save(FriendRequest.builder()
                .requester(memberA)
                .receiver(memberC)
                .status(FriendRequestStatus.PENDING)
                .build());
        friendRequestRepository.save(friendRequest);

        // soft delete 시점을 31일 전 수정
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(31);
        memberA.softDelete(deletedAt);
        diaryCommentLike.softDelete(deletedAt);
        diaryLike.softDelete(deletedAt);
        comment.softDelete(deletedAt);
        diary.softDelete(deletedAt);


        // [when] 스케쥴러를 통해 일기/댓글/좋아요 hard delete 실행
        memberService.hardDeleteMembers();

        // [then] 데이터가 완전히 삭제되었는지 검증
        assertThat(memberRepository.findById(memberA.getId())).isEmpty();
        assertThat(diaryCommentRepository.findById(comment.getId())).isEmpty();
        assertThat(diaryLikeRepository.findById(diaryLike.getId())).isEmpty();
        assertThat(diaryCommentLikeRepository.findById(diaryCommentLike.getId())).isEmpty();
        assertThat(friendRequestRepository.findById(friendRequest.getId())).isEmpty();
        assertThat(friendRepository.findById(friendship.getId())).isEmpty();
        assertThat(notificationRepository.findById(notification.getId())).isEmpty();

        assertThat(diaryRepository.findById(diary.getId())).isPresent(); // 일기는 유지되어야 함
    }

}
