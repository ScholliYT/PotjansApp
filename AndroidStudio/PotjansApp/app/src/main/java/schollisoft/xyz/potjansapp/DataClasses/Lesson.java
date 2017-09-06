package schollisoft.xyz.potjansapp.DataClasses;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Tom on 03.09.2017.
 * use Factroy Method
 */

public abstract class Lesson {
    protected String topic;

    public abstract String toString();

    public Lesson(String _topic) {
        topic = _topic;
    }

    public static Lesson createLesson(String _date, String _time, String _topic) {
        Pattern pDate = Pattern.compile("[A-Z][a-z]\\., [0-3][0-9]\\.[0-3][0-9]\\.[0-9][0-9][0-9][0-9]");

        Pattern pTime = Pattern.compile("^[0-9]\\sh$|^1[0-9]\\sh$|^2[0-3]\\sh$|^[0-9]\\.[0-6][0-9]\\sh$|^1[0-9]\\.[0-6][0-9]\\sh$|^2[0-3]\\.[0-6][0-9]\\sh$");

        if (!pDate.matcher(_date).matches() || _date.isEmpty() || !pTime.matcher(_time).matches() || _time.isEmpty()) {
            return new SpecialLesson(_date + " " +  _time, _topic);
        }

        _date = _date.substring(5);
        if(!Pattern.compile("[0-3][0-9]\\.[0-3][0-9]\\.[0-9][0-9][0-9][0-9]").matcher(_date).matches()) {
            try {
                throw new Exception("date not matching");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        _time = _time.replace(" h", "").replace(".", ":");
        if (Pattern.compile("^[0-9]$").matcher(_time).matches()) {
            _time = "0" + _time;
        }
        String completeDate = _date + " " + _time;
        Log.d("CompleteDate", completeDate);
        SimpleDateFormat sdf = new SimpleDateFormat("");
        switch (completeDate.length()) {
            case 13:
                sdf = new SimpleDateFormat("dd.MM.yyyy HH");
                break;
            case 15:
                sdf = new SimpleDateFormat("dd.MM.yyyy H:mm");
                break;
            case 16:
                sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                break;
            default:
                try {
                    throw new Exception("Wrong Time format!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        Date date = new Date();
        try {
            date = sdf.parse(completeDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new NormalLesson(date, _topic);
    }
}
