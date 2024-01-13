package com.example.driverportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

public class GetLocation extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    @SuppressLint("StaticFieldLeak")
    static TextView loc;
    private Button button3;
    private ImageView imageView;
    int REQUEST_CHECK_SETTING = 1001;
    static int REQUEST_CODE=100;
    private String busNumber;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    boolean stop = true;
    Drawable stop_img;
    Drawable start_img;
    private LocationCallback locationCallback;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        start_img = getResources().getDrawable(R.drawable.start_image);
        stop_img = getResources().getDrawable(R.drawable.stop_image);

        drawerLayout = findViewById(R.id.drawer_layout_home_activity);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar_home);
        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        Intent intent = getIntent();
        busNumber = intent.getStringExtra("busNumber");

        imageView = findViewById(R.id.start_stop_button);
        button3 = findViewById(R.id.view_map_getlocation);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).build();
        turnOnGPS();
        NotificationHelper.showNotification(this,"Starting Bus by",getTime());
        imageView.setOnClickListener(view -> {
            if(!stop){
                stop = true;
                imageView.setImageDrawable(start_img);
                stopLocationUpdates();
                Intent intent3 = new Intent(GetLocation.this, Home.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent3);
                finish();


            }
            else{
                imageView.setImageDrawable(stop_img);
                Snackbar.make(findViewById(android.R.id.content), "Please do not clear this app", Snackbar.LENGTH_LONG).show();
                stop = false;
                getCurrLocation();
            }



        });
        button3.setOnClickListener(view -> {
            Intent intent2 = new Intent(this, MapsActivity.class);
            intent2.putExtra("bus_num","1");
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            TaskStackBuilder.create(this)
//                    .addNextIntentWithParentStack(intent)
//                    .startActivities();
            startActivity(intent2);
            finish();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        stopLocationUpdates();
        if (itemId == R.id.nav_home) {
            Intent intent3 = new Intent(GetLocation.this, Home.class);
            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent3);
            finish();
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent3 = new Intent(GetLocation.this, Login.class);
            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent3);
            finish();

        }
        else if(itemId == R.id.nav_info){
//            Intent intent = new Intent(Home.this,GetLocation.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
        }

        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    private void getCurrLocation(){
        if (checkLocationPermission(this)){
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                    }
                };
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                Intent intent = new Intent(this,LocationForegroundService.class);
                intent.putExtra("busNumber",""+busNumber);
                startForegroundService(intent);
            }
            else{
                Snackbar.make(findViewById(android.R.id.content), "Please Turn on GPS!!!", Snackbar.LENGTH_LONG).show();

                stop = true;
                imageView.setImageResource(R.drawable.start_image);
                turnOnGPS();
            }
        }
        else{
            stop = true;
            imageView.setImageDrawable(start_img);
            askLocationPermission(this);
        }

    }
    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
            stopService(new Intent(this, LocationForegroundService.class));
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationUpdates();
        Intent intent = new Intent(GetLocation.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void turnOnGPS(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> Toast.makeText(GetLocation.this, "GPS Turned on", Toast.LENGTH_SHORT).show());
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(GetLocation.this,
                            REQUEST_CHECK_SETTING);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }
    private static String getTime(){
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
        return currentTime.format(formatter);
    }
    private static void askLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
    }
    private static boolean checkLocationPermission(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}