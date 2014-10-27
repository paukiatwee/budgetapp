package io.budgetapp.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Util methods
 */
public class Util {

    private Util(){}

    public static Date yearMonthDate(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date currentYearMonth() {
        LocalDate now = LocalDate.now();
        return yearMonthDate(now.getMonthValue(), now.getYear());
    }

    public static LocalDate toLocalDate(Date date) {
        Instant instant = Instant.ofEpochMilli(date.getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(String isoDate) {
        return toDate(LocalDate.parse(isoDate));
    }

    public static Date toDate(LocalDate localDate) {
        Instant instantDay = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instantDay);
    }

    public static boolean betweenDate(Date check, Date start, Date end) {
        // convert to date since we compare date only
        LocalDate checkDate = toLocalDate(check);
        LocalDate startDate = toLocalDate(start);
        LocalDate endDate = toLocalDate(end);
        return !checkDate.isBefore(startDate) && !checkDate.isAfter(endDate);
    }

    /**
     * check <code>date</code> is same year and month as <code>month</code> or not
     * @param check
     * @param month
     * @return
     */
    public static boolean inMonth(Date check, Date month) {
        LocalDate checkDate = toLocalDate(check);
        LocalDate monthDate = toLocalDate(month);
        return checkDate.getYear() == monthDate.getYear() && checkDate.getMonthValue() == monthDate.getMonthValue();
    }

    public static int yesterday(LocalDate date) {
        return date.minusDays(1).getDayOfMonth();
    }

    public static int lastWeek(LocalDate date) {
        return date.minusWeeks(1).get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }

    public static int lastMonth(LocalDate date) {
        return date.minusMonths(1).getMonthValue();
    }

    public static String toFriendlyMonthDisplay(Date date) {
        return Util.toLocalDate(date).getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }

    public static URI getDatabaseURL(String env) {
        try {
            return new URI(System.getenv(env));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
