package services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class DateService {

    /**
     * @param date   Is the Date to be returned formatted
     * @param format Is the base format to return a Date (Default is MM/dd/yyyy)
     * @return Returns the Date formatted as String
     */
    public static String getDateTimeFormat(Date date, String format) {
        if (format == null || format.length() < 8) {
            format = "MM/dd/yyyy";
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * @param daysOffset Amount in Days to offset
     * @param format     Is the base format to return a Date (Default is MM/dd/yyyy)
     * @return Returns the Date formatted as String with the amount of days added
     */
    public static String getDateOffset(int daysOffset, String format) {
        if (format == null || format.length() < 8) {
            format = "MM/dd/yyyy";
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysOffset);
        Date before = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(before);
    }

    /**
     * @param date    The Date to be modified
     * @param minutes Time in Minutes to modify the Date
     * @return Returns the Date modified in Minutes as Date
     */
    public static Date addMinutes(Date date, int minutes) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }
}