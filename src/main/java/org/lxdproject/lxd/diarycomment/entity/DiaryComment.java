package org.lxdproject.lxd.diarycomment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.BaseEntity;

@Entity
@Table(name = "diary_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DiaryComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long diaryId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String commentText;

    private Long parentId; // 일반 댓글이면 null

    private int likeCount;
}



