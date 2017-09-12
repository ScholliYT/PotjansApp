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
    public String getDateString() { return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date); }

    private boolean highlight = false;
    private void setHighlight() { highlight = true; }
    public boolean getHighlight() { return highlight; }

    public NormalLesson(Date _date, String _topic) {
        super(_topic);
        date = _date;
    }

    public static NormalLesson HighlightesLesson(Date _date, String _topic) {
        NormalLesson n = new NormalLesson(_date, _topic);
        n.setHighlight();
        return n;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(date) + "    " + topic;
    }
}
