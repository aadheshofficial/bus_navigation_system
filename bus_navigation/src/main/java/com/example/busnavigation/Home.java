package com.example.busnavigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private ProgressBar progressBar;
    Toolbar toolbar;
    private TextView textView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        retrieveDataAndUpdateListView();

        drawerLayout = findViewById(R.id.drawer_layout_home_activity);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar_home);
        progressBar = findViewById(R.id.home_progress_bar);
        textView = findViewById(R.id.no_avail_bus_home);
        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent3 = new Intent(Home.this, Login.class);
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

    private void retrieveDataAndUpdateListView() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus Location");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> busNumbers = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String busNumber = snapshot.getKey().toString();

                    busNumbers.add(busNumber);
                }

                progressBar.setVisibility(View.GONE);
                if (busNumbers.isEmpty()) {
                    // If no data received or empty data retrieved, display the textView
                    textView.setVisibility(View.VISIBLE);

                } else {
                    // Now, you have the list of bus numbers
                    textView.setVisibility(View.GONE);
                }
                updateListView(busNumbers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                Toast.makeText(Home.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListView(List<String> busNumbers) {
        BusAdapter adapter = new BusAdapter(this, busNumbers);
        ListView listView = findViewById(R.id.home_list_view);
        Log.i("bus num", busNumbers.toString());

        if (busNumbers.isEmpty()) {
            // If the list is empty, set an empty adapter to clear the ListView
            listView.setAdapter(null);
        } else {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected bus number
                    String selectedBusNumber = busNumbers.get(position);

                    // For example, you can open a new activity or show a toast
                    Log.i("select bus",selectedBusNumber);
                    Intent intent2 = new Intent(Home.this, MapsActivity.class);
                    intent2.putExtra("bus_num",""+selectedBusNumber);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent2);
                    finish();
                }
            });
        }

    }
    private static String getTime(){
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
        return currentTime.format(formatter);
    }
    private static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
}