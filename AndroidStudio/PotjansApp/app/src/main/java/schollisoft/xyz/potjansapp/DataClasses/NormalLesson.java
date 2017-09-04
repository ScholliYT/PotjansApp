package schollisoft.xyz.potjansapp.DataClasses;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tom on 03.09.2017.
 */

public class NormalLesson extends Lesson {
    private Date time;
    public Date getTime() {
        return time;
    }

    public NormalLesson(Date _time, String _topic) {
        super(_topic);
        time = _time;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(time) + "    " + topic;
    }
}
