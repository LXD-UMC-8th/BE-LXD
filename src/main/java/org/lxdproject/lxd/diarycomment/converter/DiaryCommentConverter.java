package org.lxdproject.lxd.diarycomment.converter;

import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.member.entity.Member;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiaryCommentConverter {

    public static DiaryCommentResponseDTO.Comment toCommentDto(
            DiaryComment comment,
            Long parentId,
            Set<Long> likedCommentIds,
            List<DiaryCommentResponseDTO.Comment> replies
    ) {
        Member writer = comment.getMember();

        return DiaryCommentResponseDTO.Comment.builder()
                .commentId(comment.getId())
                .parentId(parentId)
                .memberId(writer.getId())
                .username(writer.getUsername())
                .nickname(writer.getNickname())
                .profileImage(writer.getProfileImg())
                .content(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .isLiked(likedCommentIds.contains(comment.getId()))
                .createdAt(DateFormatUtil.formatDate(comment.getCreatedAt()))
                .replyCount(replies.size())
                .replies(replies)
                .build();
    }

    public static List<DiaryCommentResponseDTO.Comment> toCommentDtoTree(
            List<DiaryComment> parentComments,
            Map<Long, List<DiaryComment>> repliesGroupedByParent,
            Set<Long> likedCommentIds
    ) {
        return parentComments.stream()
                .map(parent -> {
                    List<DiaryComment> replies = repliesGroupedByParent.getOrDefault(parent.getId(), List.of());

                    List<DiaryCommentResponseDTO.Comment> replyDTOs = replies.stream()
                            .map(reply -> toCommentDto(reply, parent.getId(), likedCommentIds, List.of()))
                            .toList();

                    return toCommentDto(parent, null, likedCommentIds, replyDTOs);
                })
                .toList();
    }
}

