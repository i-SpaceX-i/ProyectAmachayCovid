package com.example.amachay.activities.Client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.amachay.R;
import com.example.amachay.activities.MainActivity;
import com.example.amachay.includes.MyToolbar;
import com.example.amachay.providers.AuthProvider;
import com.example.amachay.providers.GeofireProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Marker mMarker;

    private AuthProvider mAuthProvider;

    private GoogleMap mMap;

    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;

    private final static int SETTINGS_REQUEST_CODE = 2;

    private GeofireProvider mGeofireProvider;

    private LatLng mCurrentLatlng;

    private List<Marker> mTiendaMarkers = new ArrayList<>();

    private boolean mIsFirstTime = true;

    private AutocompleteSupportFragment mAutocomplete;

    private AutocompleteSupportFragment mAutocompleteDestination;

    private PlacesClient mPlaces;

    private String mOrigin;

    private LatLng mOriginLatlng;

    private String mDestination;

    private LatLng mDestinationLatlng;

    private GoogleMap.OnCameraIdleListener mCameraListener;

    private Button mButtonRequestTienda;


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location: locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

                    /*
                       if(mMarker != null)
                    {
                        mMarker.remove();

                    }
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(),location.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_profle))
                    );

                     */
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
// SI ES LA PRIMERA VEZ QUE ENTRA AQU
                    if(mIsFirstTime)
                    {
                        mIsFirstTime= false;
                        getActiveTienda();
                        limitSearch();

                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        MyToolbar.show(this, "Cliente", false);

        mAuthProvider = new AuthProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mGeofireProvider = new GeofireProvider();
        mButtonRequestTienda = findViewById(R.id.btnRequestTienda);



        if(!Places.isInitialized())
        {

            Places.initialize(getApplicationContext(),getResources().getString(R.string.google_maps_key));
        }
        mPlaces = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();
        mButtonRequestTienda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestTienda();
            }
        });


    }

    private void requestTienda()
    {
        if(mOriginLatlng!=null && mDestinationLatlng!=null)
        {
            Intent intent = new Intent(MapClientActivity.this,DetailRequestActivity.class);
            intent.putExtra("origin_lat",mOriginLatlng.latitude);
            intent.putExtra("origin_lng",mOriginLatlng.longitude);
            intent.putExtra("destiantion_lat",mDestinationLatlng.latitude);
            intent.putExtra("destination_lng",mDestinationLatlng.longitude);
            intent.putExtra("origin",mOrigin);
            intent.putExtra("destination",mDestination);

            startActivity(intent);
        }
        else
        {
            Toast.makeText(this,"Seleccione a dónde quiere ir ",Toast.LENGTH_SHORT).show();

        }

    }




    private void limitSearch()
    {
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatlng,5000,0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatlng,5000,180);
        mAutocomplete.setCountry("PER");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide,northSide));
        mAutocompleteDestination.setCountry("PER");
        mAutocompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide,northSide));

    }
    private  void onCameraMove()
    {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapClientActivity.this);
                    mOriginLatlng = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatlng.latitude,mOriginLatlng.longitude,1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String adress = addressList.get(0).getAddressLine(0);
                    mOrigin = adress + " " + city;
                    mAutocomplete.setText(adress + " " + city);
                }
                catch (Exception e)
                {
                    Log.d("Error: ","Mensaje error: "+e.getMessage());
                }
            }
        };


    }
    private void instanceAutocompleteOrigin()
    {
        mAutocomplete = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutocomplete.setHint("Lugar actual");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatlng =place.getLatLng();
                Log.d("PLACE","Name: "+mOrigin);
                Log.d("PLACE","Lat: "+mOriginLatlng.latitude);
                Log.d("PLACE","Lng: "+mOriginLatlng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }

    private void instanceAutocompleteDestination()
    {
        mAutocompleteDestination = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteDestination);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutocomplete.setHint("Destino");
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatlng =place.getLatLng();
                Log.d("PLACE","Name: "+mOrigin);
                Log.d("PLACE","Lat: "+mDestinationLatlng.latitude);
                Log.d("PLACE","Lng: "+mDestinationLatlng.longitude);

            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });
    }






    private void getActiveTienda()
    {

        mGeofireProvider.getActiveTienda(mCurrentLatlng).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
// AÑADIREMOS LOS MARCADORES DE LAS TIENDAS QUE SE CONECTEN EN LA APLICACION

                for (Marker marker: mTiendaMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng tiendaLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(tiendaLatLng).title("Tienda disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tienda2)));
                marker.setTag(key);
                mTiendaMarkers.add(marker);


            }

            @Override
            public void onKeyExited(String key) {
                // VAMOS A ELIMINAR LOS MARCADORES DE LOS CONDUCTORES QUE SE DESCONECTAN
                for (Marker marker: mTiendaMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mTiendaMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // ACTUALIZAR LA POSICION DE CADA CONDUCTOR
                for (Marker marker: mTiendaMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            //LE DECIMOS QUE SE ESTABLEZCA EN UNA NUEVA POSICI{ON CON SETPOSITION
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }

        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Para establecer el punto de nuestra ubicación exacta

        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                    else {
                        showAlertDialogNOGPS();
                    }
                }
                else {
                    checkLocationPermissions();
                }
            }
            else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived())
        {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
        else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActived())
        {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.tiendamenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_logout)
        {
            logout();


        }
        return super.onOptionsItemSelected(item);
    }
    void logout()
    {
        mAuthProvider.logout();
        Intent intent = new Intent(MapClientActivity.this,MainActivity.class);
        startActivity(intent);

        finish();
    }
}
