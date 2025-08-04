package org.lxdproject.lxd.member.dto;

import lombok.*;
import org.lxdproject.lxd.diary.entity.enums.Language;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LanguageChangeResponseDTO {
    private Language systemLanguage;
}
