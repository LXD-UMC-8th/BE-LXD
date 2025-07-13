package org.lxdproject.lxd.correction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.correction.entity.mapping.MemberSavedCorrection;
import org.lxdproject.lxd.diary.entity.Diary;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id", nullable = false)
//    private Member author;

    // 교정 부분 내용 (html 포함)
    @Column(name = "content_html", columnDefinition = "TEXT", nullable = false)
    private String contentHtml;

    // 교정에 대한 코멘트 내용
    @Column(name = "comment_text", length = 500)
    private String commentText;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberSavedCorrection> savedByMembers = new ArrayList<>();
}