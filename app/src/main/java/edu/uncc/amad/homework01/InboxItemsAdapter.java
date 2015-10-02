package edu.uncc.amad.homework01;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by AswathiAjay on 10/2/2015.
 */
public class InboxItemsAdapter extends ArrayAdapter<ParseObject> {

    private Context context;
    private int resource;
    private List<ParseObject> messages;

    public InboxItemsAdapter(Context context, int resource, List<ParseObject> messages) {
        super(context, resource, messages);
        this.context = context;
        this.resource = resource;
        this.messages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);
        }
        ParseObject message = messages.get(position);
        ParseUser sender = (ParseUser) message.get("sender");
        ((TextView) convertView.findViewById(R.id.senderTextView)).setText(String.format("%s %s", sender.getString("firstName"), sender.getString("lastName")));
        ((TextView) convertView.findViewById(R.id.regionTextView)).setText(message.getString("location"));
        String dateString = SimpleDateFormat.getInstance().format(message.getCreatedAt());
        ((TextView) convertView.findViewById(R.id.timeTextView)).setText(dateString);
        ImageView isReadIV = (ImageView) convertView.findViewById(R.id.readUnreadImageView);
        if(message.getBoolean("isRead")){
            isReadIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_grey));
        }else{
            isReadIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_blue));
        }
        ImageView isLockedIV = (ImageView) convertView.findViewById(R.id.lockedUnlockedImageView);
        if(message.getBoolean("isLocked")){
            isLockedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.lock));
        }else{
            isLockedIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.lock_open));
        }
        return convertView;
    }
}
