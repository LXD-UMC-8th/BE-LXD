package org.lxdproject.lxd.notification.message;

import java.util.Locale;

public record LocalizedMessageTemplate(String ko, String en) {

    public String format(String username, String diaryTitle, Locale locale) {
        boolean isKorean = Locale.KOREAN.getLanguage().equals(locale.getLanguage());

        if (ko.contains("%s") && diaryTitle != null) {
            return isKorean
                    ? String.format(ko, username, diaryTitle)
                    : String.format(en, username, diaryTitle);
        } else {
            return isKorean
                    ? String.format(ko, username)
                    : String.format(en, username);
        }
    }
}
