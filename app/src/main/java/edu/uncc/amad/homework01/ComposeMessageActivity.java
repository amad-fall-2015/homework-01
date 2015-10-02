package edu.uncc.amad.homework01;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Call this class with the extras "recepientUserName" and "selectedRegion" to prepopulate
 * the to and region fields
 */
public class ComposeMessageActivity extends AppCompatActivity {

    private ParseUser recepient;
    private String selectedRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            //block editing to and region fields
            findViewById(R.id.recepientImageView).setEnabled(false);
            findViewById(R.id.regionImageView).setEnabled(false);

            String recepientUserName = intent.getExtras().getString("recepientUserName");
            selectedRegion = intent.getExtras().getString("selectedRegion");
            ((TextView) findViewById(R.id.regionTextView)).setText(selectedRegion);
            ParseQuery<ParseUser> recepientQuery = ParseUser.getQuery();
            recepientQuery.whereEqualTo("username", recepientUserName);
            recepientQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e != null) {
                        Log.e("hw1", "Error fetching recepient", e);
                        return;
                    }
                    recepient = object;
                    ((TextView) findViewById(R.id.recepientTextView)).setText(String.format("%s %s", recepient.get("firstName"), recepient.get("lastName")));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose_message, menu);
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

    public void selectRecipientClicked(View view) {
        ParseQuery<ParseUser> usersQuery = ParseUser.getQuery();
        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e("hw1", "Error fetching users", e);
                    return;
                }
                final CharSequence[] usersCharSequence = new CharSequence[users.size()];
                int i = 0;
                for (ParseUser user : users) {
                    usersCharSequence[i++] = String.format("%s %s", user.get("firstName"), user.get("lastName"));
                }
                new AlertDialog.Builder(ComposeMessageActivity.this).setTitle("Users").setItems(usersCharSequence, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recepient = users.get(which);
                        ((TextView) findViewById(R.id.recepientTextView)).setText(usersCharSequence[which]);
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
            }
        });
    }

    public void selectRegionClicked(View view) {
        ParseQuery<ParseObject> regionQuery = ParseQuery.getQuery("Beacon");
        regionQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> beacons, ParseException e) {
                if (e != null) {
                    Log.e("hw1", "Error fetching regions", e);
                    return;
                }
                final CharSequence[] regionsCharSequence = new CharSequence[beacons.size()];
                int i = 0;
                for (ParseObject beacon : beacons) {
                    regionsCharSequence[i++] = String.format("%s", beacon.get("location"));
                }
                new AlertDialog.Builder(ComposeMessageActivity.this).setTitle("Regions").setItems(regionsCharSequence, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedRegion = regionsCharSequence[which].toString();
                        ((TextView) findViewById(R.id.regionTextView)).setText(regionsCharSequence[which]);
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
            }
        });
    }

    public void sendMessageClicked(View view) {
        String messageText = ((EditText) findViewById(R.id.messageEditText)).getText().toString();
        if (recepient == null || selectedRegion == null || messageText.length() == 0) {
            Toast.makeText(ComposeMessageActivity.this, "Recepient, region, message shouldn't be null", Toast.LENGTH_SHORT).show();
            return;
        }
        ParseObject message = new ParseObject("Message");
        message.put("sender", ParseUser.getCurrentUser());
        message.put("recepient", this.recepient);
        message.put("isRead", false);
        message.put("location", this.selectedRegion);

        message.put("text", messageText);
        message.put("isLocked", true);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e("hw1", "Error saving message", e);
                    return;
                }
                Toast.makeText(ComposeMessageActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                findViewById(R.id.messageEditText).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        });
    }
}
