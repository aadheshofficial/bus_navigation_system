package com.example.driverportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

public class LocationForegroundService extends Service {
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    int i = 0;
    private String busNumber ;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification(""+getTime());
        if (intent != null) {
            String data = intent.getStringExtra("busNumber");
            if (data != null) {
                busNumber = data;
            }
        }
        requestLocationUpdates();
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    private void processLocationUpdate(@NonNull Location location) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String time = getTime();
        Log.i("background location","live location :lat" + latitude + "lon" + longitude + " time :" + time + " i" + i);
        i++;
        updateBusLocation(""+busNumber,""+currentUserId(),""+latitude,""+longitude,""+getTime());

        showNotification(""+time);
    }
    private void showNotification(String content){
        Intent notificationIntent = new Intent(this, GetLocation.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Update")
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2,notification);
    }
    private void requestLocationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    processLocationUpdate(location);
                }
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }
    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        stopDatabaseUpdates();
        stopSelf();

    }
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void stopDatabaseUpdates() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Bus Location").child(""+busNumber);
        databaseReference.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Deletion successful
                        Log.d("Firebase", "Value deleted successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("Firebase", "Error deleting value: " + e.getMessage());
                    }
                });
        HashMap<String,Object> map = new HashMap<>();
        map.put("route",busNumber);
        FirebaseDatabase.getInstance().getReference().child("available_bus").child(""+busNumber).updateChildren(map);

    }
    private static String getTime(){
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
        return currentTime.format(formatter);
    }
    private static void updateBusLocation(String busNo, String driverId, String latitude, String longitude, String time) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("driver_id", driverId);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("time", time);
        map.put("status","active");
        FirebaseDatabase.getInstance().getReference().child("Bus Location").child(busNo).updateChildren(map);
    }
    private static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

}