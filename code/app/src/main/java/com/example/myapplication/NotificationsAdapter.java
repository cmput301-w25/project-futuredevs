package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.database.IResultListener;
import com.futuredevs.models.items.Notification;
import com.futuredevs.models.items.UserSearchResult;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends ArrayAdapter<Notification> {
    private final ArrayList<Notification> notifications;
    private final Context context;
    private final String currentUsername;
    public NotificationsAdapter(Context context, ArrayList<Notification> notifications, String currentUsername) {
        super(context, 0, notifications);
        this.notifications = notifications;
        this.context = context;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        Notification notification = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notifications, parent, false);
        }

        TextView notificationTextView = convertView.findViewById(R.id.notification_text);
        Button acceptButton = convertView.findViewById(R.id.notification_accept_button);
        Button declineButton = convertView.findViewById(R.id.notification_decline_button);

        notificationTextView.setText(notification.getSourceUsername() + " has requested to follow");

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the Database instance's sendFollowRequest method.
                Database.getInstance().acceptFollowRequest(notification, new IResultListener() {
                    @Override
                    public void onResult(DatabaseResult result) {
                        if (result == DatabaseResult.SUCCESS) {
                            Toast.makeText(context, "Follow request accepted", Toast.LENGTH_SHORT).show();
                            notifications.remove(notification);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Cannot accept follow request at this moment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the Database instance's sendFollowRequest method.
                Database.getInstance().rejectFollowingRequest(notification, new IResultListener() {
                    @Override
                    public void onResult(DatabaseResult result) {
                        if (result == DatabaseResult.SUCCESS) {
                            Toast.makeText(context, "Follow request declined", Toast.LENGTH_SHORT).show();
                            notifications.remove(notification);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Cannot decline follow request at this moment", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        return convertView;
    }
}
