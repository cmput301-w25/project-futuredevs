package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.models.ViewModelNotifications;
import com.futuredevs.models.items.Notification;

import java.util.ArrayList;


/**
 * A Fragment that displays a list of notification items.
 * This fragment listens to changes in the ModelNotifications and updates its ListView accordingly.
 * It creates an instance of ModelNotifications using the current user's username and registers itself
 * as a listener for data changes.
 */
public class NotificationsFragment extends Fragment {
	private NotificationsAdapter notificationsAdapter;
    private ArrayList<Notification> notificationsList;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the layout, initializes the ListView and its adapter,
     * and sets the adapter to the ListView to display the notifications.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     *
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications, container, false);
		ListView listView = view.findViewById(R.id.notification_list);

        TextView emptyNotificationsText = view.findViewById(R.id.empty_notifications_text);
        listView.setEmptyView(emptyNotificationsText);

        if (this.getActivity() != null) {
            ViewModelNotifications viewModelNotifications
                    = new ViewModelProvider(this.getActivity()).get(ViewModelNotifications.class);
            viewModelNotifications.getData().observe(this.getViewLifecycleOwner(), o -> {
                notificationsList.clear();
                notificationsList.addAll(o);
                notificationsAdapter.notifyDataSetChanged();
            });

            notificationsList = new ArrayList<>();
            notificationsAdapter = new NotificationsAdapter(getContext(), viewModelNotifications, notificationsList);
            listView.setAdapter(notificationsAdapter);
        }

        return view;

    }
}
