package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

public class NotificationsFragment extends Fragment implements IModelListener<Notification> {

    private ListView listView;
    private NotificationsAdapter notificationsAdapter;
    private ArrayList<Notification> notificationsList;
    // Replace with the logged-in user's username.
    private ModelNotifications modelNotifications;
    private final String currentUsername = Database.getInstance().getCurrentUser();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create the model instance with the current username.
        modelNotifications = new ModelNotifications(currentUsername);
        // Register this fragment as a listener to the model.
        modelNotifications.addChangeListener(this);
        modelNotifications.requestData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.notifications, container, false);
        listView = view.findViewById(R.id.notification_list);

        notificationsList = new ArrayList<>();
        notificationsAdapter = new NotificationsAdapter(getContext(), notificationsList, currentUsername);
        listView.setAdapter(notificationsAdapter);

        return view;

    }

    /**
     * @param theModel the model that was changed
     */
    @Override
    public void onModelChanged(ModelBase<Notification> theModel) {
        List<Notification> results = modelNotifications.getModelData();
        notificationsList.clear();
        notificationsList.addAll(results);
        notificationsAdapter.notifyDataSetChanged();
    }
}
