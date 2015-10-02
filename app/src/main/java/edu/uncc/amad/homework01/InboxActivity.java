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

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private List<ParseObject> messages = new ArrayList<>();
    private ArrayAdapter<ParseObject> adapter;
    private ProgressDialog progressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        progressDialogue = new ProgressDialog(this);
        progressDialogue.setTitle("Loading Results");
        progressDialogue.setCancelable(false);
        progressDialogue.show();
        this.adapter = new InboxItemsAdapter(this, R.layout.inbox_row_item, this.messages);
        this.adapter.setNotifyOnChange(true);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(this.adapter);
        ParseQuery<ParseObject> messagesQuery = ParseQuery.getQuery("Message");
        messagesQuery.whereEqualTo("recepient", ParseUser.getCurrentUser());
        messagesQuery.include("sender");
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
        if(id == R.id.action_compose){
            Intent compose = new Intent(this, ComposeMessageActivity.class);
            startActivity(compose);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
