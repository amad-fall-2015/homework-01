package edu.uncc.amad.homework01;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InboxActivity extends AppCompatActivity {

    private List<ParseObject> messages = new ArrayList<>();
    private ArrayAdapter<ParseObject> adapter;
    private ProgressDialog progressDialogue;
    private BeaconManager beaconManager;
    private Region region;
    private Map<String, ParseObject> registeredBeacons;
    private Map<String, Integer> beaconsInRange;
    private static final int THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        registeredBeacons = new HashMap<>();
        beaconsInRange = new HashMap<>();

        ParseQuery<ParseObject> beaconsQuery = ParseQuery.getQuery("Beacon");
        beaconsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e("hw1", "Error fetching registered beacons", e);
                    return;
                }
                for(ParseObject beacon : objects){
                    registeredBeacons.put(String.format("%s:%s", String.valueOf(beacon.getInt("major")),
                            String.valueOf(beacon.getInt("minor"))), beacon);
                }
            }
        });

        beaconManager = new BeaconManager(this);
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon beacon = list.get(0);
                    String key = String.format("%s:%s", String.valueOf(beacon.getMajor()),
                            String.valueOf(beacon.getMinor()));
                    if (beaconsInRange.containsKey(key)) {
                        if (beaconsInRange.get(key) == THRESHOLD - 1) {
                            beaconsInRange.clear();
                            unlockMessages(key);
                        } else {
                            beaconsInRange.put(key, beaconsInRange.get(key) + 1);
                        }
                    } else {
                        beaconsInRange.put(key, 0);
                    }
                } else {
                    Log.d("hw1", "out of range");
                    beaconsInRange.clear();
                    Toast.makeText(getBaseContext(), "Out of range", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.adapter = new InboxItemsAdapter(this, R.layout.inbox_row_item, this.messages);
        this.adapter.setNotifyOnChange(true);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(this.adapter);

    }

    private void fetchMessages() {

        progressDialogue = new ProgressDialog(this);
        progressDialogue.setTitle("Loading Inbox");
        progressDialogue.setCancelable(false);
        progressDialogue.show();

        ParseQuery<ParseObject> messagesQuery = ParseQuery.getQuery("Message");
        messagesQuery.whereEqualTo("recepient", ParseUser.getCurrentUser());
        messagesQuery.include("sender");
        messagesQuery.orderByDescending("createdAt");
        messagesQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e("hw1", "Error fetching messages", e);
                    return;
                }
                messages.clear();
                messages.addAll(objects);
                adapter.notifyDataSetChanged();
                progressDialogue.dismiss();
            }
        });
    }

    private void unlockMessages(String key) {
        Toast.makeText(getBaseContext(), "Unlocking region : " + registeredBeacons.get(key).getString("location"), Toast.LENGTH_SHORT).show();
        ParseQuery<ParseObject> unlockMsgsQuery = ParseQuery.getQuery("Message");
        unlockMsgsQuery.whereEqualTo("recepient", ParseUser.getCurrentUser());
        unlockMsgsQuery.whereEqualTo("isLocked", true);
        unlockMsgsQuery.whereEqualTo("location", registeredBeacons.get(key).getString("location"));
        unlockMsgsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects == null || objects.size() == 0){
                    Toast.makeText(getBaseContext(), "Nothing to unlock", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(ParseObject message : objects){
                    message.put("isLocked", false);
                }
                ParseObject.saveAllInBackground(objects, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e("hw1", "Error unlocking messages", e);
                            return;
                        }
                        fetchMessages();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inbox, menu);
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
        if (id == R.id.action_compose) {
            Intent compose = new Intent(this, ComposeMessageActivity.class);
            startActivity(compose);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        fetchMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.stopRanging(region);
    }
}
