package com.example.busnavigation;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.busnavigation.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    String busNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent1 = getIntent();
        busNumber = intent1.getStringExtra("bus_num").toString();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationAvailability(mMap);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Bus Location").child(""+busNumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                // Access latitude and longitude
                String latitude = snapshot.child("latitude").getValue(String.class);
                String longitude = snapshot.child("longitude").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                if (latitude != null && longitude != null){
                    LatLng location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_marker);
                    mMap.addMarker(new MarkerOptions().position(location).icon(customMarker).title("Bus "+busNumber));
//10.757400414678804, 78.6513221804935
                    LatLng college = new LatLng(10.757400414678804, 78.6513221804935);
                    BitmapDescriptor collegeMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker);
                    mMap.addMarker(new MarkerOptions().position(college).icon(collegeMarker).title("Saranathan College of Engineering"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,16),2000,null);

                }
                if (latitude == null && longitude == null && status == "active") {
                    Snackbar.make(binding.getRoot(), "Your Bus has not yet started", Snackbar.LENGTH_SHORT).show();

                }
                else if (latitude == null && longitude == null ) {
                    Snackbar.make(binding.getRoot(), "Your Bus ride has been ended", Snackbar.LENGTH_SHORT).show();

                }
            }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    LatLng college = new LatLng(10.757400414678804, 78.6513221804935);
                    BitmapDescriptor collegeMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker);
                    mMap.addMarker(new MarkerOptions().position(college).icon(collegeMarker).title("Saranathan College of Engineering"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(college,16),2000,null);
                    Snackbar.make(binding.getRoot(), "Error loading the live location", Snackbar.LENGTH_SHORT).show();
                }
            });

    }

    private void checkLocationAvailability(GoogleMap mMap) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Bus Location").child(""+busNumber);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The child node exists
                    // You can now access the data using dataSnapshot.getValue()
                } else {
                    // The child node does not exist
                    LatLng college = new LatLng(10.757400414678804, 78.6513221804935);
                    BitmapDescriptor collegeMarker = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_marker);
                    mMap.addMarker(new MarkerOptions().position(college).icon(collegeMarker).title("Saranathan College of Engineering"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(college,16),2000,null);
                    Snackbar.make(binding.getRoot(), "Error loading the live location", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
//        Log.i("map","fault");
//        FirebaseDatabase.getInstance().getReference().child("Bus Location").child(""+busNumber).get()
//                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if(!task.isSuccessful()){
//
//                }
//            }
//        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapsActivity.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}