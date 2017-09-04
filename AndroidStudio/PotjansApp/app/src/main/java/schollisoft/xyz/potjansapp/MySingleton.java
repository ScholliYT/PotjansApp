package schollisoft.xyz.potjansapp;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

/**
 * Created by Tom on 04.09.2017.
 */

public class MySingleton {
    private static MySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private MySingleton(Context _ctx) {
        ctx = _ctx;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null) {

            Cache cache = new DiskBasedCache(ctx.getCacheDir(), 1024 * 1024); //1MB
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            requestQueue = new RequestQueue(cache, network);
            requestQueue.start();


        }
        return requestQueue;
    }

    public static synchronized MySingleton getInstance(Context _ctx) {
        if(instance == null) {
            instance = new MySingleton(_ctx);
        }
        return instance;
    }

    public void addToRequestQueue(Request _request) {
        requestQueue.add(_request);
    }

}
