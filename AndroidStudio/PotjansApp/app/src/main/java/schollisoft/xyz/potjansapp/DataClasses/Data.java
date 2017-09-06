package schollisoft.xyz.potjansapp.DataClasses;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tom on 05.09.2017.
 */

public class Data {
    private HashMap<String, JSONLesson[]> locations;

    public HashMap<String, JSONLesson[]> getLocations() {
        return locations;
    }

    public String toString() {
        String s = "";
        for(Map.Entry<String, JSONLesson[]> e : locations.entrySet()) {
            s += e.getKey() + ": \n\n";
            for (JSONLesson t : e.getValue()) {
               s += t.toString();
            }
        }
        return s;
    }
}
