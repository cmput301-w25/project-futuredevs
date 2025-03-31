package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.futuredevs.database.DatabaseResult;
import com.futuredevs.models.ViewModelNotifications;
import com.futuredevs.models.items.Notification;

import java.util.ArrayList;

/**
 * An ArrayAdapter for displaying notification items in a ListView.
 * Each notification represents a follow request and includes the source user's username,
 * along with buttons to either accept or decline the follow request.
 * Upon accepting or declining, the adapter updates the view accordingly.
 *
 * @author Pranav Gupta
 */
public class NotificationsAdapter extends ArrayAdapter<Notification> {
    private final ViewModelNotifications notifModel;
    private final Context context;

    /**
     * Constructs a new NotificationsAdapter.
     *
     * @param context         the current context
     * @param notifications   the list of Notification objects to display
     */
    public NotificationsAdapter(Context context, ViewModelNotifications notifModel,
                                ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifModel = notifModel;
    }

    /**
     * Provides a view for an AdapterView (ListView) by inflating the layout and populating it
     * with the notification data for a given position in the list.
     *
     * @param position    the position of the item within the adapter's data set
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent that this view will eventually be attached to
     * @return a View corresponding to the data at the specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        Notification notification = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notifications, parent, false);
        }

        TextView notificationTextView = convertView.findViewById(R.id.notification_text);
        ImageButton acceptButton = convertView.findViewById(R.id.notification_accept_button);
        ImageButton declineButton = convertView.findViewById(R.id.notification_decline_button);

        notificationTextView.setText(notification.getSourceUsername() + " has requested to follow");

        acceptButton.setOnClickListener(view -> {
            notifModel.acceptFollowRequest(notification, r -> {
                if (r == DatabaseResult.SUCCESS) {
                    Toast.makeText(context, "Follow request accepted", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
                else {
                    Toast.makeText(context, "Cannot accept follow request at this moment", Toast.LENGTH_SHORT).show();
                }
            });
        });

        declineButton.setOnClickListener(view -> {
            notifModel.rejectFollowRequest(notification, r -> {
                if (r == DatabaseResult.SUCCESS) {
                    Toast.makeText(context, "Follow request declined", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Cannot decline follow request at this moment", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }
}
