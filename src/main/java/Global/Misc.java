package Global;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Misc {
    public static int getNumbersOnly(String myString) {
        return Integer.parseInt(myString.replaceAll("\\D+", ""));
    }

    public static JSONObject fromStringToJson(String myString) {
        JSONObject jsonObject = new JSONObject(myString);
        return jsonObject;
    }

    public static String getDateOfTheDay() {
        LocalDate localDate = LocalDate.now();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
    }

    public static long getDifferenceBetweenTwoStringDateInDays(String date1, String date2) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = sdf.parse(date1);
        Date secondDate = sdf.parse(date2);

        long diff = secondDate.getTime() - firstDate.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
}