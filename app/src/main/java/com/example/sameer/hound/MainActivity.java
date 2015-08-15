package com.example.sameer.hound;

import com.loopj.android.http.*;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    int my_pin = 0;
    int tracking_pin = 0;
    int countDown = 0;
    String friendName = null;

    Boolean stop_tracking = false;
    Boolean locationSharingStarted = false;
    Boolean firstRenderOnMap = true;
    int Numboftabs = 2;

    CountDownTimer timer;
    private Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[] = {"Friends", "You"};
    MarkerOptions friend_marker = new MarkerOptions();
    AsyncHttpClient client = new AsyncHttpClient();

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
                firstRenderOnMap = true;
                tracking_pin = (Integer.parseInt(input_pin.getText().toString()));
                friendName = input_friend_name.getText().toString();
                dialog.cancel();
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
                    if (obj.has("error")) {
                        showInvalidPinDialog();
                    } else {
                        double latitude = obj.getDouble("latitude");
                        double longitude = obj.getDouble("longitude");
                        addMapMarker(latitude, longitude);

                        if (latitude != 0 && longitude != 0 && stop_tracking == false) {
                            showLocation();
                        }
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

    public void addMapMarker(double latitude, double longitude) {
        GoogleMap map = ((map) adapter.getRegisteredFragment(0)).map;

        LatLng friend_location = new LatLng(latitude, longitude);
        map.clear();
        map.setMyLocationEnabled(true);

        friend_marker.position(friend_location);
        friend_marker.title(friendName);
        map.addMarker(friend_marker);
        if (firstRenderOnMap) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(friend_location, 15));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
            firstRenderOnMap = false;
        }
    }

    public void stopTracking(MenuItem menu) {
        stop_tracking = true;
        GoogleMap map = ((map) adapter.getRegisteredFragment(0)).map;
        map.clear();
        Toast.makeText(getApplicationContext(), "Stopped tracking!", Toast.LENGTH_LONG).show();
    }

    public void openGeneratePinDialog(View menu) {
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
                int expiration = input_time.getValue();
                client.get(MainActivity.baseURL + "/getPin/" + expiration, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response, "UTF-8"));
                            my_pin = obj.getInt("pin");
                            countDown = obj.getInt("countDown");
                            startSharingLocation();
                        } catch (UnsupportedEncodingException e) {
                        } catch (JSONException e) {
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
        TextView pin_view = (TextView) findViewById(R.id.textView3);
        com.gc.materialdesign.views.ButtonRectangle generate_pin_button = (com.gc.materialdesign.views.ButtonRectangle) findViewById(R.id.generate_pin_button);
        TextView countDownTimer = (TextView) findViewById(R.id.countDownTimer);
        Button copyToClipboardButton = (Button) findViewById(R.id.copyToClipboardButton);
        Button share_pin_button = (Button) findViewById(R.id.share_pin_button);
        com.gc.materialdesign.views.ButtonRectangle destroyPinButton = (com.gc.materialdesign.views.ButtonRectangle) findViewById(R.id.destroyPin);

        generate_pin_button.setVisibility(View.INVISIBLE);
        countDownTimer.setVisibility(View.VISIBLE);
        share_pin_button.setVisibility(View.VISIBLE);
        copyToClipboardButton.setVisibility(View.VISIBLE);
        destroyPinButton.setVisibility(View.VISIBLE);
        pin_view.setText(" " + my_pin);
        showTimer(countDown);
        Toast.makeText(getApplicationContext(), "Your Pin: " + my_pin, Toast.LENGTH_LONG).show();
        locationSharingStarted = true;
        Intent intent = new Intent(this, LocationSharingService.class);
        intent.putExtra("pin", my_pin + "");
        startService(intent);

    }

    public void stopSharingLocation() {
        cancelTimer();
        com.gc.materialdesign.views.ButtonRectangle generatePinButton = (com.gc.materialdesign.views.ButtonRectangle) findViewById(R.id.generate_pin_button);
        Button sharePinButton = (Button) findViewById(R.id.share_pin_button);
        Button copyToClipboard = (Button) findViewById(R.id.copyToClipboardButton);
        com.gc.materialdesign.views.ButtonRectangle destroyPin = (com.gc.materialdesign.views.ButtonRectangle) findViewById(R.id.destroyPin);
        TextView countDownTimer = (TextView) findViewById(R.id.countDownTimer);
        TextView showPin = (TextView) findViewById(R.id.textView3);

        showPin.setText("No pin generated :(");
        countDownTimer.setVisibility(View.INVISIBLE);
        generatePinButton.setVisibility(View.VISIBLE);
        sharePinButton.setVisibility(View.INVISIBLE);
        copyToClipboard.setVisibility(View.INVISIBLE);
        destroyPin.setVisibility(View.INVISIBLE);

        locationSharingStarted = false;
        Intent intent = new Intent(this, LocationSharingService.class);
        stopService(intent);
        Toast.makeText(getApplicationContext(), "Location sharing stopped!", Toast.LENGTH_LONG).show();
    }

    public void sharePin(View v) {
        String shareText = "My Hound Pin is: " + my_pin;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Pin"));
    }

    public void copyPinToClipboard(View v) {
        String shareText = "My Hound Pin is: " + my_pin;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Hound Pin", shareText);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Pin copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    public void destroyPin(View v) {
        stopSharingLocation();
        sendPinExpiryMessage();
    }

    public void showTimer(int countDown) {
        timer = new CountDownTimer(countDown, 1000) {
            TextView countDownTimer = (TextView) findViewById(R.id.countDownTimer);

            public void onTick(long millisUntilFinished) {
                if (locationSharingStarted == false) {
                    cancelTimer();
                    countDownTimer.setText("");
                    return;
                } else {
                    long hours = millisUntilFinished / (60 * 60 * 1000);
                    long minutes = (millisUntilFinished % (60 * 60 * 1000)) / (60 * 1000);
                    long seconds = (millisUntilFinished % (60 * 1000) / 1000);
                    StringBuilder time = new StringBuilder();
                    if (hours > 0) {
                        time.append(hours + ":");
                    }
                    if (minutes > 9)
                        time.append(minutes + ":");
                    else
                        time.append("0" + minutes + ":");

                    if (seconds > 9)
                        time.append(seconds);
                    else
                        time.append("0" + seconds);
                    countDownTimer.setText("Pin expires in: " + time + " sec");
                }
            }

            public void onFinish() {
                countDownTimer.setText("Pin expired!");
            }
        }.start();
    }

    public void cancelTimer() {
        timer.cancel();
    }

    public void showInvalidPinDialog() {
        Toast.makeText(this, "Pin you entered is invalid or expired", Toast.LENGTH_SHORT).show();
    }

    public void sendPinExpiryMessage() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(MainActivity.baseURL + "/destroyPin/" + my_pin, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                my_pin = 0;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {

            }

            @Override
            public void onRetry(int retryNo) {

            }
        });
    }
}
