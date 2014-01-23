package dk.nsi.haiba.fgrimporter.model;

import java.util.Calendar;
import java.util.Date;

public class Util {
    public static final Date THE_END_OF_TIME = toDate(2999, 12, 31);

    public static Date toDate(int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, date);
        return cal.getTime();
    }
}
