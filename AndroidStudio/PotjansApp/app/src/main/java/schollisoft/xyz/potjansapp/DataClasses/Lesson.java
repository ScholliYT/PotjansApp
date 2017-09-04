package schollisoft.xyz.potjansapp.DataClasses;

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

    public static Lesson createLesson(String _time, String _topic) {
        Pattern p = Pattern.compile("^[0-9]\\sh$|^1[0-9]\\sh$|^2[0-3]\\sh$|^[0-9]\\.[0-6][0-9]\\sh$|^1[0-9]\\.[0-6][0-9]\\sh$|^2[0-3]\\.[0-6][0-9]\\sh$");

        if (!p.matcher(_time).matches() || _time.isEmpty()) {
            return new SpecialLesson(_time, _topic);
        }

        _time = _time.replace(" h", "").replace(".", ":");
        if (Pattern.compile("^[0-9]$").matcher(_time).matches()) {
            _time = "0" + _time;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("");
        switch (_time.length()) {
            case 2:
                sdf = new SimpleDateFormat("HH");
                break;
            case 4:
                sdf = new SimpleDateFormat("H:mm");
                break;
            case 5:
                sdf = new SimpleDateFormat("HH:mm");
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
            date = sdf.parse(_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new NormalLesson(date, _topic);
    }
}
