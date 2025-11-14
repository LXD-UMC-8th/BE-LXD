package org.lxdproject.lxd.domain.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lxdproject.lxd.domain.diary.entity.enums.Language;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDTO {
    private Long id;
    private String content;
    private Language language;
}
