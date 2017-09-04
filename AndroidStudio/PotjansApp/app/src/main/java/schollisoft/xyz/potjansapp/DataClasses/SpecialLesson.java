package schollisoft.xyz.potjansapp.DataClasses;

/**
 * Created by Tom on 03.09.2017.
 */

public class SpecialLesson extends Lesson {
    private String info;
    public SpecialLesson(String _info, String _topic) {
        super(_topic);
        info = _info;
    }

    public String toString() {
        return info + " " + topic;
    }
}
