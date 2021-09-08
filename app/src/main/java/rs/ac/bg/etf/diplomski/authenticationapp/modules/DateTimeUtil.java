package rs.ac.bg.etf.diplomski.authenticationapp.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    private static final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("dd.MM.yyyy.");

    private static final SimpleDateFormat simpleDateTimeFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final Calendar calendar =
            Calendar.getInstance();

    public static SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public static SimpleDateFormat getSimpleDateTimeFormat() {
        return simpleDateTimeFormat;
    }

    public static String realMinutesToString(double realMinutes) {
        int minutes = (int) realMinutes;
        int seconds = (int) (60 * (realMinutes - minutes));
        return minutes + ":" + String.format("%02d", seconds);
    }

    public static Date getDate(int year, int month, int day) {
        calendar.set(year, month, day);
        return calendar.getTime();
    }
}
