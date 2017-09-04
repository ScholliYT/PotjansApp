package schollisoft.xyz.potjansapp;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import schollisoft.xyz.potjansapp.DataClasses.Lesson;

public class MainActivity extends AppCompatActivity {

    private String server_url;

    private HashMap<String, Lesson[]> lessons = new HashMap<>();
    private String[] locations = new String[0];
    private int currentDisplayed = 0;

    private ListView listView;
    private TextView txtLocation;
    private LinearLayout llDots;
    private LinearLayout llMain;
    private SwipeRefreshLayout sw_refresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server_url = getResources().getString(R.string.server_url);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }

        sw_refresh = (SwipeRefreshLayout) findViewById(R.id.sw_refresh);

        sw_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //code for updating screen
                reloadData();
            }
        });


        txtLocation = (TextView) findViewById(R.id.txtLocation);
        listView = (ListView) findViewById(R.id.lv_lessons);
        llDots = (LinearLayout) findViewById(R.id.llDots);
        llMain = (LinearLayout) findViewById(R.id.llMain);

        setupSwipeControls();

        //Check if new Data is needed ? download data : continue
        //TODO: Dynamic
        locations = new String[]{"Dülmen", "Buldern", "Hausdülmen"};

        //TODO: Dynamic
        lessons.put(locations[0], new Lesson[]{Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("7 h", getResources().getString(R.string.topic) + ": 3"), Lesson.createLesson("9.30 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("15 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("19.30 h", getResources().getString(R.string.topic) + ": 6")});
        lessons.put(locations[1], new Lesson[]{Lesson.createLesson("8 h", getResources().getString(R.string.topic) + ": 4"), Lesson.createLesson("10.30 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("16 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("20.30 h", getResources().getString(R.string.topic) + ": 7")});
        lessons.put(locations[2], new Lesson[]{Lesson.createLesson("9 h", getResources().getString(R.string.topic) + ": 5"), Lesson.createLesson("11.30 h", getResources().getString(R.string.topic) + ": 6"), Lesson.createLesson("17 h", getResources().getString(R.string.topic) + ": 7"), Lesson.createLesson("21 h", getResources().getString(R.string.topic) + ": 8")});

        //Display local data
        displayData(lessons.get(locations[currentDisplayed]));

    }

    private void reloadData() {

        sw_refresh.setRefreshing(true);

        RequestQueue requestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); //1MB
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        // Request a string response from the provided URL.
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, server_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("Response: ", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //following line is important to stop animation for refreshing
                        sw_refresh.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "An Error just crashed the whole app...");
                error.printStackTrace();
                //following line is important to stop animation for refreshing
                sw_refresh.setRefreshing(false);
            }
        });
        // Add the request to the RequestQueue.
        requestQueue.add(getRequest);
    }

    private void search(String query) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_refresh:
                reloadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayData(Lesson[] _lessons) {
        txtLocation.setText(locations[currentDisplayed]);
        llDots.removeAllViews();
        for(int dot = 0; dot < locations.length; dot++) {
            if(dot == currentDisplayed) {
                ImageView ivDarkDot = new ImageView(this);
                ivDarkDot.setImageResource(R.drawable.dot);
                llDots.addView(ivDarkDot);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivDarkDot.getLayoutParams();
                marginLayoutParams.setMargins(10,5,10,10);
            }
            else {
                ImageView ivBrightDot = new ImageView(this);
                ivBrightDot.setImageResource(R.drawable.dot_bright);
                llDots.addView(ivBrightDot);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivBrightDot.getLayoutParams();
                marginLayoutParams.setMargins(10,5,10,10);
            }
        }

        String[] data = new String[_lessons.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = _lessons[i].toString();
        }
        addEnteriesToListView(data);
    }

    private void addEnteriesToListView(String[] _values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, _values);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
    }

    private void setupSwipeControls() {
        llMain.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {

            }

            public void onSwipeRight() {
                if (currentDisplayed == 0) {
                    currentDisplayed = lessons.size() - 1;
                } else {
                    currentDisplayed--;
                }
                displayData(lessons.get(locations[currentDisplayed]));
                //Toast.makeText(MainActivity.this, "right " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeLeft() {
                if (currentDisplayed == locations.length - 1) {
                    currentDisplayed = 0;
                } else {
                    currentDisplayed++;
                }
                displayData(lessons.get(locations[currentDisplayed]));
                //Toast.makeText(MainActivity.this, "left " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeBottom() {

            }
        });
        listView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {

            }

            public void onSwipeRight() {
                if (currentDisplayed == 0) {
                    currentDisplayed = lessons.size() - 1;
                } else {
                    currentDisplayed--;
                }
                displayData(lessons.get(locations[currentDisplayed]));
                //Toast.makeText(MainActivity.this, "right " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeLeft() {
                if (currentDisplayed == locations.length - 1) {
                    currentDisplayed = 0;
                } else {
                    currentDisplayed++;
                }
                displayData(lessons.get(locations[currentDisplayed]));
                //Toast.makeText(MainActivity.this, "left " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeBottom() {

            }
        });
    }
}