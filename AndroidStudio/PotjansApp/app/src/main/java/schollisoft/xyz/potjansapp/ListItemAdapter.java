package schollisoft.xyz.potjansapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

import schollisoft.xyz.potjansapp.DataClasses.Lesson;
import schollisoft.xyz.potjansapp.DataClasses.NormalLesson;
import schollisoft.xyz.potjansapp.DataClasses.SpecialLesson;

/**
 * Created by Tom on 07.09.2017.
 */

public class ListItemAdapter extends BaseAdapter {

    Context context;
    Lesson[] data;
    private static LayoutInflater inflater = null;

    public ListItemAdapter(Context _context, Lesson[] _data) {
        context = _context;
        data = _data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v("ListAdapter", "Position: " + position + " convertView: " + convertView);
        final View v;
        if(convertView == null) {
            v = inflater.inflate(R.layout.listitem, null);
        }
        else {
            v = convertView;
        }

        if(data[position] instanceof NormalLesson) {
            TextView txtDate = (TextView) v.findViewById(R.id.txtdate);
            txtDate.setText(((NormalLesson)data[position]).getDateString());

            TextView txtTopic = (TextView) v.findViewById(R.id.txttopic);
            txtTopic.setText(data[position].getTopic());

            if(((NormalLesson)data[position]).getHighlight()) {
                v.setBackgroundColor(Color.RED);
            }
            else {
                v.setBackgroundColor(Color.TRANSPARENT);
            }

        }
        else if((data[position] instanceof SpecialLesson)) {
            String info = ((SpecialLesson)data[position]).getInfo();
            if(!info.isEmpty()) {
                TextView txtDate = (TextView) v.findViewById(R.id.txtdate);
                txtDate.setText(info);
            }

            String topic = (data[position]).getTopic();
            if(!topic.isEmpty()) {
                TextView txtTopic = (TextView) v.findViewById(R.id.txttopic);
                txtTopic.setText(topic);
            }
        }
        else {
            try {
                throw new Exception("No valid class!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return v;
    }
}
