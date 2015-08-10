package com.example.sameer.hound;

import com.loopj.android.http.*;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.text.InputType;
import android.content.DialogInterface;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {
    final static String baseURL = "https://hound-trackerweb.rhcloud.com";
    int tracking_pin = 0;
    String friendName = null;
    Boolean stop_tracking = false;
    Boolean locationSharingStarted = false;
    int my_pin = 0;
    Boolean firstRenderOnMap = true;
    private Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Friends", "You"};
    int Numboftabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getSupportActionBar().show();
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void showAddTrackingDialog(MenuItem menu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add friend");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // Set up the input
        final EditText input_pin = new EditText(this);
        final EditText input_friend_name = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input_pin.setInputType(InputType.TYPE_CLASS_NUMBER);
        input_pin.setHint(R.string.pin_hint);

        input_friend_name.setInputType(InputType.TYPE_CLASS_TEXT);
        input_friend_name.setHint(R.string.name_hint);

        layout.addView(input_friend_name);
        layout.addView(input_pin);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tracking_pin = (Integer.parseInt(input_pin.getText().toString()));
                friendName = input_friend_name.getText().toString();
                dialog.cancel();
                firstRenderOnMap = true;
                if (tracking_pin != 0) {
                    showLocation();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
    }

    public void showLocation() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(MainActivity.baseURL + "/getLocation/" + tracking_pin, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String str = new String(response, "UTF-8");
                    JSONArray friend_locations = new JSONArray(str);
                    System.out.println("Updated! " + str);
                    JSONObject obj = friend_locations.getJSONObject(0);
                    System.out.println("Debug " + obj.toString());
                    double latitude = obj.getDouble("latitude");
                    double longitude = obj.getDouble("longitude");

                    GoogleMap map = ((map) adapter.getRegisteredFragment(0)).map;

                    LatLng friend_location = new LatLng(latitude, longitude);
                    map.addMarker(new MarkerOptions().position(friend_location).title(friendName));
                    if (firstRenderOnMap) {
                        map.moveCamera(CameraUpdateFactory.newLatLng(friend_location));
                        map.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                        firstRenderOnMap = false;
                    }
                    if (latitude != 0 && longitude != 0 && stop_tracking == false) {
                        showLocation();
                    }
                } catch (JSONException jsonExcep) {
                    System.out.println(jsonExcep);
                } catch (UnsupportedEncodingException unsuppEncodingExcep) {
                    System.out.println(unsuppEncodingExcep);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onRetry(int retryNo) {

            }
        });
    }

    public void stopTracking(MenuItem menu) {
        stop_tracking = true;
        Toast.makeText(getApplicationContext(), "Stopped tracking!", Toast.LENGTH_LONG).show();
    }

    public void openGeneratePinDialog(MenuItem menu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Generate pin");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView label_input_time = new TextView(this);
        final NumberPicker input_time = new NumberPicker(this);

        input_time.setMaxValue(120);
        input_time.setMinValue(15);
        input_time.setEnabled(true);
        label_input_time.setText(R.string.tracking_time_message);
        label_input_time.setPadding(100, 50, 100, 50);
        layout.addView(label_input_time);
        layout.addView(input_time);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(MainActivity.baseURL + "/getPin/10" + tracking_pin, new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        try {
                            String str = new String(response, "UTF-8");
                            System.out.println("Updated! " + str);
                            JSONObject obj = new JSONObject(str);
                            my_pin = obj.getInt("pin");
                            Toast.makeText(getApplicationContext(), "Your Pin: " + my_pin, Toast.LENGTH_LONG).show();
                            startSharingLocation();
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void startSharingLocation() {
        locationSharingStarted = true;
        Intent intent = new Intent(this, LocationSharingService.class);
        intent.putExtra("pin", my_pin + "");
        startService(intent);

    }

    public void stopSharingLocation() {
        locationSharingStarted = false;
        Intent intent = new Intent(this, LocationSharingService.class);
        stopService(intent);
        Toast.makeText(getApplicationContext(), "Location sharing stopped!", Toast.LENGTH_LONG).show();


    }

    public void toggleLocationSharing(MenuItem menu) {
        if (locationSharingStarted) {
            stopSharingLocation();
            menu.setIcon(R.drawable.ic_gps_fixed_white_24dp);
        } else {
            openGeneratePinDialog(menu);
            menu.setIcon(R.drawable.ic_location_disabled_white_24dp);
        }
    }
}
