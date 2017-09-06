package schollisoft.xyz.potjansapp.DataClasses;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tom on 03.09.2017.
 */

public class NormalLesson extends Lesson {
    private Date date;
    public Date getDate() {
        return date;
    }

    public NormalLesson(Date _date, String _topic) {
        super(_topic);
        date = _date;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(date) + "    " + topic;
    }
}
