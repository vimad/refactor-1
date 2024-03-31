package org.example;

import org.apache.commons.lang3.ObjectUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DateUtil {

    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter MM_YYYY = DateTimeFormatter.ofPattern("MM/yyyy");
    public static final DateTimeFormatter M_D_U = DateTimeFormatter.ofPattern("M/d/u");
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Australia/Sydney");

    public static List<String> getDates(String startDate, String endDate,
                                        DateTimeFormatter formatter) {
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        ArrayList<String> dates = new ArrayList<>();

        for (LocalDate date = start; date.isBefore(end)
                || date.isEqual(end); date = date.plusDays(1)) {
            String dateString = formatter.format(date);
            dates.add(dateString);
        }

        return dates;
    }


    public static String getFormattedCurrentDate(String formatPattern) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Australia/Sydney"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return currentDate.format(formatter);
    }

    public static String getFormattedPreviousDate(String formatPattern, int yearsBack, int monthsBack, int daysBack) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Australia/Sydney"));
        Period periodToSubtract = Period.ofYears(yearsBack).plusMonths(monthsBack).plusDays(daysBack);
        LocalDate prevDate = currentDate.minus(periodToSubtract);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return prevDate.format(formatter);
    }

    public static String convertStringFormat(String sourceDateString, String sourceFormatPattern, String targetFormatPattern) {
        DateTimeFormatter sourceFormatter = DateTimeFormatter.ofPattern(sourceFormatPattern);
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern(targetFormatPattern);
        LocalDate sourceDate = LocalDate.parse(sourceDateString, sourceFormatter);

        return sourceDate.format(targetFormatter);
    }

    /**
     * expects month string with yyyyMM
     *
     * @param sourceMonthString   202308
     * @param targetFormatPattern dd/MM/yyyy
     * @return 01/08/2023 to 31/08/2023
     */
    public static String getMonthPeriodFormat(String sourceMonthString, String targetFormatPattern) {
        int year = Integer.parseInt(sourceMonthString.substring(Constants.Date.ZERO, Constants.Date.FOUR));
        int monthValue = Integer.parseInt(sourceMonthString.substring(Constants.Date.FOUR, Constants.Date.SIX));
        YearMonth yearMonth = YearMonth.of(year, monthValue);
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern(targetFormatPattern);
        return yearMonth.atDay(1).format(targetFormatter) + " to " + yearMonth.atEndOfMonth().format(targetFormatter);
    }

    public static String getFormattedDateString(String sourceDateString, String sourceFormatPattern, String targetFormatPattern) {
        if (!Objects.isNull(sourceDateString) && ObjectUtils.isNotEmpty(sourceDateString)) {
            try {
                return DateUtil.convertStringFormat(sourceDateString, sourceFormatPattern, targetFormatPattern);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return sourceDateString;
    }

    public static String parseISODateString(String isoDateString) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateString, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return  outputFormatter.format(dateTime);
        } catch (DateTimeParseException e) {
            System.out.println(e);
        }
        return isoDateString;
    }
}
