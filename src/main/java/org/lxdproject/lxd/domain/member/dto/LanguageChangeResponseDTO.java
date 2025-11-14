package org.lxdproject.lxd.domain.member.dto;

import lombok.*;
import org.lxdproject.lxd.domain.diary.entity.enums.Language;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LanguageChangeResponseDTO {
    private Language systemLanguage;
}
