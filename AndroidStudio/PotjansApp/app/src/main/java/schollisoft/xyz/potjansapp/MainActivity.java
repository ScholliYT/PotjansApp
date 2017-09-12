package schollisoft.xyz.potjansapp;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import schollisoft.xyz.potjansapp.DataClasses.Data;
import schollisoft.xyz.potjansapp.DataClasses.JSONLesson;
import schollisoft.xyz.potjansapp.DataClasses.Lesson;
import schollisoft.xyz.potjansapp.DataClasses.NormalLesson;

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


        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            reloadData();
        }
    }

    private void reloadData() {
        Log.v("ReloadData ", "Reloading new data");
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
                        Gson gson = new Gson();
                        Data data = gson.fromJson(response.toString(), Data.class);
                        Log.v("Response Length: ", response.toString().length() + " chars");
                        Log.v("Data", data.toString());
                        ArrayList<String> tempLocations = new ArrayList<>();
                        for (Map.Entry<String, JSONLesson[]> e : data.getLocations().entrySet()) {
                            tempLocations.add(e.getKey());
                            ArrayList<Lesson> tempLesson = new ArrayList<>();
                            for (JSONLesson jl : e.getValue()) {
                                tempLesson.add(Lesson.createLesson(jl.getDate(), jl.getTime(), jl.getTopic()));
                            }
                            Lesson[] tempLessonArray = new Lesson[tempLesson.size()];
                            lessons.put(e.getKey(), tempLesson.toArray(tempLessonArray));
                        }
                        locations = tryToSort(tempLocations);
                        for (String s : locations) {
                            Log.v("Locations", s);
                        }
                        //Display data
                        displayData(lessons.get(locations[currentDisplayed]), findNextLessonToHighlight(lessons.get(locations[currentDisplayed])));


                        //following line is important to stop animation for refreshing
                        sw_refresh.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                String message = null;
                if (volleyError instanceof NetworkError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof ServerError) {
                    message = "The server could not be found. Please try again after some time!!";
                } else if (volleyError instanceof AuthFailureError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof ParseError) {
                    message = "Parsing error! Please try again after some time!!";
                } else if (volleyError instanceof NoConnectionError) {
                    message = "Cannot connect to Internet...Please check your connection!";
                } else if (volleyError instanceof TimeoutError) {
                    message = "Connection TimeOut! Please check your internet connection.";
                }
                Log.e("Error", message);
                txtLocation.setText("Error");
                llDots.removeAllViews();
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
                volleyError.printStackTrace();

                //following line is important to stop animation for refreshing
                sw_refresh.setRefreshing(false);
            }
        });
        // Add the request to the RequestQueue.
        MySingleton.getInstance(this).getRequestQueue().add(getRequest);
    }

    private String[] tryToSort(ArrayList<String> _locations) {
        if(_locations.contains("dlm") && _locations.contains("bln") && _locations.contains("hdl")) {
            return new String[] { "dlm", "bln", "hdl" };
        }
        return _locations.toArray(new String[_locations.size()]);
    }

    private void search(String query) {
        Log.d("Search", "Query: " + query);
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

    private void displayData(Lesson[] _lessons, int _highlight) {
        final int highlight = _highlight;
        final int offset = getResources().getInteger(R.integer.highlight_offset);

        txtLocation.setText(getFullName(locations[currentDisplayed]));
        llDots.removeAllViews();
        for (int dot = 0; dot < locations.length; dot++) {
            if (dot == currentDisplayed) {
                ImageView ivDarkDot = new ImageView(this);
                ivDarkDot.setImageResource(R.drawable.dot);
                llDots.addView(ivDarkDot);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivDarkDot.getLayoutParams();
                marginLayoutParams.setMargins(10, 5, 10, 10);
            } else {
                ImageView ivBrightDot = new ImageView(this);
                ivBrightDot.setImageResource(R.drawable.dot_bright);
                llDots.addView(ivBrightDot);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) ivBrightDot.getLayoutParams();
                marginLayoutParams.setMargins(10, 5, 10, 10);
            }
        }

        _lessons[highlight] = NormalLesson.HighlightesLesson(((NormalLesson)_lessons[highlight]).getDate(), _lessons[highlight].getTopic());
        addEnteriesToListView(_lessons);


        if (highlight - offset >= 0) {
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        view.setOnScrollListener(null);

                        // Fix for scrolling bug
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                view.setSelection(highlight - offset);
                            }
                        });
                    }
                }

                @Override
                public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                final int totalItemCount) { }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPositionFromTop(highlight, offset);
                }
            });
        }
    }

    private void addEnteriesToListView(Lesson[] _lessons) {
        ListItemAdapter lia = new ListItemAdapter(this, _lessons);
        // Assign adapter to ListView
        listView.setAdapter(lia);
    }

    private String getFullName(String location) {
        int checkExistence = getResources().getIdentifier(location, "string", this.getPackageName());

        if (checkExistence != 0) {  // the resouce exists...
            return getResources().getString(checkExistence);
        } else {  // checkExistence == 0  // the resouce does NOT exist!!
            try {
                throw new Exception("Not in locations");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return location;
        }
    }

    private int findNextLessonToHighlight(Lesson[] _lessons) {
        long minTimeDiff = Long.MAX_VALUE;
        int minTimeDiffIndex = -1;
        Date currentDate = new Date();
        for (int i = 0; i < _lessons.length; i++) {
            if (_lessons[i] instanceof NormalLesson) {
                long timeDiff = ((NormalLesson) _lessons[i]).getDate().getTime() - currentDate.getTime();
                if (timeDiff < minTimeDiff && timeDiff > 0) {
                    minTimeDiff = timeDiff;
                    minTimeDiffIndex = i;
                }
            }
        }
        return minTimeDiffIndex;
    }



    private void setupSwipeControls() {
        OnSwipeTouchListener oswl = new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {

            }

            public void onSwipeRight() {
                if (currentDisplayed == 0) {
                    currentDisplayed = lessons.size() - 1;
                } else {
                    currentDisplayed--;
                }
                displayData(lessons.get(locations[currentDisplayed]), findNextLessonToHighlight(lessons.get(locations[currentDisplayed])));
                //Toast.makeText(MainActivity.this, "right " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeLeft() {
                if (currentDisplayed == locations.length - 1) {
                    currentDisplayed = 0;
                } else {
                    currentDisplayed++;
                }
                displayData(lessons.get(locations[currentDisplayed]), findNextLessonToHighlight(lessons.get(locations[currentDisplayed])));
                //Toast.makeText(MainActivity.this, "left " + currentDisplayed, Toast.LENGTH_SHORT).show();

            }

            public void onSwipeBottom() {

            }
        };

        llMain.setOnTouchListener(oswl);
        listView.setOnTouchListener(oswl);
    }
}
