package org.lxdproject.lxd.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.mapping.DiaryLike;
import org.lxdproject.lxd.member.entity.enums.LoginType;
import org.lxdproject.lxd.member.entity.enums.Role;
import org.lxdproject.lxd.member.entity.enums.Status;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class Member{

    // 고유번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주사용언어
    @Enumerated(EnumType.STRING)
    @Column(name = "native_language", nullable = false)
    private Language nativeLanguage;

    // 학습언어
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    // 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 아이디
    @Column(nullable = false, length = 20)
    private String username;

    // 비밀번호
    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    // 이메일
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    // 닉네임
    @Column(nullable = false, length = 20, unique = true)
    private String nickname;

    // 로그인 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    // 개인정보 약관 동의 여부
    @Column(name = "is_privacy_agreed", nullable = false)
    private Boolean isPrivacyAgreed;

    // 프로필 이미지 URL
    @Column(name = "profile_img", columnDefinition = "TEXT")
    private String profileImg;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // 알림 설정 여부
    @Column(name = "is_alarm_agreed", nullable = false)
    private Boolean isAlarmAgreed;

    // 일기 연관관계 설정
    @OneToMany(mappedBy = "member")
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<DiaryLike> diaryLikes = new ArrayList<>();

}
