package org.lxdproject.lxd.diary.entity.enums;

import java.util.Locale;

public enum Language {
    KO, ENG;

    public Locale toLocale() {
        return switch (this) {
            case KO -> Locale.KOREAN;
            case ENG -> Locale.ENGLISH;
        };
    }

    public static Language fromLocale(Locale locale) {
        if (locale == null) return KO;
        return switch (locale.getLanguage()) {
            case "ko" -> KO;
            case "en" -> ENG;
            default -> ENG; // 기본 영어 처리
        };
    }
}
