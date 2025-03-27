package com.example.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NetworkActivity extends AppCompatActivity {

    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkCallback();
    }

    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;
        NetworkRequest request = new NetworkRequest.Builder().build();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Run on main thread to show toast
                runOnUiThread(() -> Toast.makeText(NetworkActivity.this, "Online Mode", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onLost(Network network) {
                // Run on main thread to show toast
                runOnUiThread(() -> Toast.makeText(NetworkActivity.this, "Offline Mode", Toast.LENGTH_SHORT).show());
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback);
        } else {
            cm.registerNetworkCallback(request, networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);

        }
    }
}
