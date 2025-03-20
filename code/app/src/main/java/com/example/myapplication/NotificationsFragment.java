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

import com.futuredevs.database.Database;
import com.futuredevs.models.IModelListener;
import com.futuredevs.models.ModelBase;
import com.futuredevs.models.ModelNotifications;
import com.futuredevs.models.items.Notification;
import com.futuredevs.models.items.UserSearchResult;

import java.util.ArrayList;
import java.util.List;


/**
 * A Fragment that displays a list of notification items.
 * This fragment listens to changes in the ModelNotifications and updates its ListView accordingly.
 * It creates an instance of ModelNotifications using the current user's username and registers itself
 * as a listener for data changes.
 */
public class NotificationsFragment extends Fragment implements IModelListener<Notification> {

    private ListView listView;
    private NotificationsAdapter notificationsAdapter;
    private ArrayList<Notification> notificationsList;
    // Replace with the logged-in user's username.
    private ModelNotifications modelNotifications;
    private final String currentUsername = Database.getInstance().getCurrentUser();

    /**
     * Called when the fragment is created.
     * This method initializes the model for notifications using the current username,
     * registers this fragment as a change listener, and requests the initial set of data.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create the model instance with the current username.
        modelNotifications = new ModelNotifications(currentUsername);
        // Register this fragment as a listener to the model.
        modelNotifications.addChangeListener(this);
        modelNotifications.requestData();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the layout, initializes the ListView and its adapter,
     * and sets the adapter to the ListView to display the notifications.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.notifications, container, false);
        listView = view.findViewById(R.id.notification_list);

        TextView emptyNotificationsText = view.findViewById(R.id.empty_notifications_text);
        listView.setEmptyView(emptyNotificationsText);

        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(getContext(), notificationsList, currentUsername);
        listView.setAdapter(notificationsAdapter);

        return view;

    }

    /**
     * Called when the model data has changed.
     * This method updates the notifications list with the new data from the model
     * and notifies the adapter to refresh the ListView.
     *
     * @param theModel The model that was changed.
     */
    @Override
    public void onModelChanged(ModelBase<Notification> theModel) {
        List<Notification> results = modelNotifications.getModelData();
        notificationsList.clear();
        notificationsList.addAll(results);
        notificationsAdapter.notifyDataSetChanged();
    }
}
