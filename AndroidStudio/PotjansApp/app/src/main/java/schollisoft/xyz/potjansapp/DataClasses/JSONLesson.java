package schollisoft.xyz.potjansapp.DataClasses;

/**
 * Created by Tom on 06.09.2017.
 */

public class JSONLesson {
    private String time;
    private String topic;
    private String date;

    public String getTime() {
        return time;
    }
    public String getTopic() {
        return topic;
    }
    public String getDate() {
        return date;
    }


    public String toString() {
        return date + " " + time + ": " + topic + "\n";
    }
}
