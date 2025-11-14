package org.lxdproject.lxd.domain.diary.util;

public class DiaryUtil {

    private static final int PREVIEW_LENGTH = 175;

    public static String generateContentPreview(String content) {
        if (content == null) return "";
        return content.length() <= PREVIEW_LENGTH ? content : content.substring(0, PREVIEW_LENGTH);
    }
}
