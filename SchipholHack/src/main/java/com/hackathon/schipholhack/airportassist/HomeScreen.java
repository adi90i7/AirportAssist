package com.hackathon.schipholhack.airportassist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.gson.Gson;
import org.altbeacon.beacon.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class HomeScreen extends ActionBarActivity implements BeaconConsumer {

    protected static final String TAG = "AdiTest";
    private BeaconManager beaconManager;
    private WebView webView;
    List<String> beaconAddress = new ArrayList<String>();
    private static Context mContext;
    public static String beaconid;
    public static String majorid;
    public static String minorID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        mContext = getApplicationContext();


        this.webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebInterface(this), "testCall");
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Finished loading URL: " +url);

            }
        });
        webView.loadUrl("https://adi90i7.github.io/");



        beaconManager = BeaconManager.getInstanceForApplication(this);
       /* beaconManager.setBackgroundBetweenScanPeriod(3000);
        beaconManager.setBackgroundScanPeriod(10000);
        beaconManager.setForegroundScanPeriod(5000);
        beaconManager.setForegroundBetweenScanPeriod(10000);*/
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {


                Log.i(TAG,"Enter aagudhu");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG,"poidchu");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.i(TAG,"State changes"+i);
            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons.size()>0) {
                    Beacon beacon = beacons.iterator().next();
                    if (!beacon.getBluetoothName().equals("OnyxBeacon")) {

                        //  if(beacon.)
                        beaconid = beacon.getId1().toString().toUpperCase();
                        majorid = beacon.getId2().toString();
                        minorID = beacon.getId3().toString();

                        new testCallAPI().execute();

                        //  beaconAddress.add(beacons.iterator().next().getBluetoothAddress());
                        //  Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");

                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notification = new Notification(R.mipmap.ic_launcher,"Sometext",System.currentTimeMillis());

                        Intent notificationIntent = new Intent(mContext, HomeScreen.class);

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent intent = PendingIntent.getActivity(mContext, 0,
                                notificationIntent, 0);

                        notification.setLatestEventInfo(mContext, "SomeSubject", "SOmeBody", intent);
                        notification.flags |= Notification.FLAG_AUTO_CANCEL;
                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        notificationManager.notify(0, notification);
                    }
                }
                //int beaconsize = beacons.size();
               // Log.i(TAG,"BeaconSize"+beaconsize);

            }
        });


        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.i(TAG,"SomeExcceptipn");
        }
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private class testCallAPI extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            StringBuilder builder = new StringBuilder();
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet("http://airportassistanceandoffer.scalingo.io/airports/productsofferandsuggestions?beaconUUID=B233860B-5F9D-44EF-B657-C2966D89BBFA&flightnr=DL0165&appId=XXXXX");
            String text = null;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));


                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONArray object = new JSONArray(builder.toString());
                for (int i=0; i < object.length(); i++) {

                    JSONObject obj = object.getJSONObject(i);


                }
               // Log.i(TAG,object.getString("id"));


                //text = getASCIIContentFromEntity(entity);
            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
            return builder.toString();

        }
    }

    public class WebInterface{
        Context mcontext;
        WebInterface(Context m){
            mcontext =m;
        }
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }



}
