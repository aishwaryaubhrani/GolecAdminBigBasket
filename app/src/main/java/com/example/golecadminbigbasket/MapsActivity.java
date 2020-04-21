package com.example.golecadminbigbasket;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private EditText address;
    private Button create;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        address = findViewById(R.id.editText4);
        create = findViewById(R.id.button6);
        sharedPreferences = getSharedPreferences("com.example.golecadminbigbasket", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        final String driver_name = sharedPreferences.getString("driver_name", "null");
        final String driver_password = sharedPreferences.getString("driver_password", "null");
        final String driver_phone_number = sharedPreferences.getString("driver_phone_number", "null");

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("drivers");

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String client_address = address.getText().toString();
                if(client_address.isEmpty()){
                    address.setError("Required");
                    address.requestFocus();
                    return;
                }
                String latitude = sharedPreferences.getString("latitude", "0");
                String longitude = sharedPreferences.getString("longitude", "0");
                setDataIntoDatabase(driver_phone_number, driver_name, driver_password, client_address, latitude, longitude);
                Toast.makeText(MapsActivity.this, "Driver Created Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        address.bringToFront();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

    }

    private void setDataIntoDatabase(String driver_phone_number, String driver_name, String driver_password, String address, String latitude, String longitude) {
        databaseReference.child(driver_phone_number).child("Driver Name").setValue(driver_name);
        databaseReference.child(driver_phone_number).child("Driver Password").setValue(driver_password);
        databaseReference.child(driver_phone_number).child("Warehouse Location").setValue(address);
        databaseReference.child(driver_phone_number).child("Warehouse Latitude").setValue(latitude);
        databaseReference.child(driver_phone_number).child("Warehouse Longitude").setValue(longitude);
    }

    private void fetchLastLocation() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Task<Location> task = fusedLocationProviderClient.getLastLocation();
                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    currentLocation = location;
                                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                                    supportMapFragment.getMapAsync(MapsActivity.this);
                                }
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must accept this permission.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F));
        editor.putString("latitude", Double.toString(currentLocation.getLatitude()));
        editor.putString("longitude", Double.toString(currentLocation.getLongitude()));
        editor.commit();

        getCompleteAddress(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F));
                editor.putString("latitude", Double.toString(latLng.latitude));
                editor.putString("longitude", Double.toString(latLng.longitude));
                editor.commit();
                //getting address from lat lng.
                getCompleteAddress(latLng.latitude, latLng.longitude);
            }
        });
    }

    private void getCompleteAddress(double latitude, double longitude) {
        String your_address = "";
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(your_address!=null){
                Address returnAddress = addresses.get(0);
                StringBuilder stringBuilder = new StringBuilder("");
                for(int i = 0; i<=returnAddress.getMaxAddressLineIndex(); i++){
                    stringBuilder.append(returnAddress.getAddressLine(i)).append(",");
                }
                your_address = stringBuilder.toString();

            }
            else {
                Toast.makeText(MapsActivity.this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(MapsActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

        address.setText(your_address);
    }
}
