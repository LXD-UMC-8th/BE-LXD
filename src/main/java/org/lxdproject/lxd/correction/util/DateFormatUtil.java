package org.lxdproject.lxd.correction.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateFormatUtil {
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy. MM. dd a hh:mm", Locale.KOREA));
    }
}
