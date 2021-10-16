package com.yusufdagdeviren.mapjava.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.yusufdagdeviren.mapjava.R;
import com.yusufdagdeviren.mapjava.databinding.ActivityMapsBinding;
import com.yusufdagdeviren.mapjava.model.Place;
import com.yusufdagdeviren.mapjava.room.PlaceDao;
import com.yusufdagdeviren.mapjava.room.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ActivityResultLauncher<String> activityResultLauncher;
    private SharedPreferences sharedPreferences;
    private boolean info;
    private PlaceDatabase placeDatabase;
    private PlaceDao placeDao;
    private double selectLatitude;
    private double selectLongitude;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int position;
    private double getLatitude;
    private double getLongitude;
    private List<Place> placeListi;
    private AlertDialog.Builder alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = MapsActivity.this.getSharedPreferences("com.yusufdagdeviren.mapjava",MODE_PRIVATE);
        info = false;
        placeDatabase = Room.databaseBuilder(MapsActivity.this,PlaceDatabase.class,"Place")
                //.allowMainThreadQueries()
                .build();
        placeDao = placeDatabase.placeDao();
        selectLatitude = 0.0;
        selectLongitude = 0.0;
        alert = new AlertDialog.Builder(MapsActivity.this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        position = intent.getIntExtra("int",-1);
        if(info.equals("new")){

        binding.deleteButton.setVisibility(View.GONE);


        }else{
            binding.saveButton.setVisibility(View.GONE);
            compositeDisposable.add(placeDao.getAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::getData));


        }





    }
    public void getData(List<Place> placeList){
            placeListi = placeList;
            binding.nameText.setText(placeList.get(position).name);
            getLatitude = placeList.get(position).latitude;
            getLongitude = placeList.get(position).longitude;
            LatLng latLng = new LatLng(getLatitude,getLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));
            mMap.addMarker(new MarkerOptions().position(latLng).title(placeList.get(position).name));
    }
   
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        binding.saveButton.setEnabled(false);

       locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
       locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

        info = sharedPreferences.getBoolean("info",false);
        if(!info){

            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));
            sharedPreferences.edit().putBoolean("info",true).apply();


        }



            }

        };


        if(ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){


            if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){

                Snackbar.make(binding.getRoot(),"Permission needed for lcoation",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                    activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);


                    }
                }).show();


            }else{
                activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }


        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(lastLocation != null){

                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,15f));

            }
            mMap.setMyLocationEnabled(true);

        }


    }


    public void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){

                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(lastLocation != null){

                            LatLng lastLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,15f));

                        }
                    }



                }else{
                    Toast.makeText(MapsActivity.this,"No Permission !!", Toast.LENGTH_LONG).show();


                }



            }
        });




    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        selectLatitude = latLng.latitude;
        selectLongitude = latLng.longitude;
        binding.saveButton.setEnabled(true);
    }

    public void save(View view){


        //  Threading --> Main(UI), Default(Cpu), IO(Network, database)
        // placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); buda yapılabilir ama alttaki öneriliyor



        alert.setTitle("Kayit");
        alert.setMessage("Kaydedeceginize emin misiniz?");
        alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Place place = new Place(binding.nameText.getText().toString(),selectLatitude,selectLongitude);

                compositeDisposable.add(placeDao.insert(place)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(MapsActivity.this::handleResponse)



                );


            }
        });
        alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                startActivity(intent);

            }
        });

        alert.show();


        //placeDao.insert(place);

    }
    public void delete(View view){

        alert.setTitle("Sil");
        alert.setMessage("Sileceginize emin misiniz?");
        alert.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                /*if(placeListi != null){
                    compositeDisposable.add(placeDao.delete(placeListi.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(MapsActivity.this::handleResponse)

                    );*/
                if(placeListi != null){


                    compositeDisposable.add(placeDao.delete(placeListi.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(MapsActivity.this::handleResponse));



                }



            }
        });
        alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                startActivity(intent);

            }
        });

        alert.show();



















    }





    private void handleResponse(){

        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}