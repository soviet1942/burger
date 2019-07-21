package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Date转String，默认格式 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String defaultDateFormat(Date date) {
        return dateFormat(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * Date转String
     * @param date
     * @return
     */
    public static String dateFormat(Date date, DateFormat dateFormat) {
        String res = dateFormat.format(date);
        return res;
    }
}
