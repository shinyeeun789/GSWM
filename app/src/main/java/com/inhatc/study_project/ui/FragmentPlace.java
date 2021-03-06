package com.inhatc.study_project.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inhatc.study_project.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class FragmentPlace extends Fragment implements OnMapReadyCallback, PlacesListener {
    private FragmentActivity mContext;
    private TextView placeType;
    private Button searchCafe, searchLibrary;

    private static final String TAG = FragmentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private MapView mapView = null;
    private Marker currentMarker = null;

    private FusedLocationProviderClient mFusedLocationProviderClient; // Deprecated??? FusedLocationApi??? ??????
    private LocationRequest locationRequest;
    private Location mCurrentLocation;
    private LatLng currentPosition;

    private final LatLng mDefaultLocation = new LatLng(37.56, 126.97);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private static final int UPDATE_INTERVAL_MS = 1000 * 60 * 1;  // 1??? ?????? ?????? ??????
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000 * 30 ; // 30??? ????????? ?????? ??????

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    List<Marker> previous_marker = new ArrayList<Marker>();

    @Override
    public void onAttach(Activity activity) { // Fragment ??? Activity??? attach ??? ??? ????????????.
        mContext =(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ????????? ?????? ?????? ??????????????? ????????? ????????? ?????????.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Layout ??? inflate ?????? ?????????.
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        View view =  inflater.inflate(R.layout.fragment_place,container,false);
        mapView = (MapView)view.findViewById(R.id.googleMap);
        if(mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
        mapView.getMapAsync(this);
        placeType = (TextView) view.findViewById(R.id.tv_place);
        searchCafe = (Button) view.findViewById(R.id.btnSCafe);
        searchCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType.setText("[  CAFE  ]");
                showPlaceCafe();
            }
        });
        searchLibrary = (Button) view.findViewById(R.id.btnSLibrary);
        searchLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType.setText("[  LIBRARY  ]");
                showPlaceLibrary();
            }
        });
        return view;
    }

    @Override
    public void onPlacesFailure(PlacesException e) { }

    @Override
    public void onPlacesStart() { }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(noman.googleplaces.Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);
                }

                // ?????? ?????? ??????
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
            }
        });
    }

    @Override
    public void onPlacesFinished() { }

    public void showPlaceCafe() {
        mMap.clear();           // ?????? ?????????

        if(previous_marker != null) {
            previous_marker.clear();
        }
        new NRPlaces.Builder()
                .listener(FragmentPlace.this)
                .key("AIzaSyAOp6FjpfM32AZ2aiobjruHBtxGTu2sNMk")
                .latlng(currentPosition.latitude, currentPosition.longitude)      // ?????? ??????
                .radius(500)                                                      // 500 ?????? ????????? ??????
                .type(PlaceType.CAFE)                                             // ??????
                .build().execute();
    }

    public void showPlaceLibrary() {
        mMap.clear();           // ?????? ?????????

        if(previous_marker != null) {
            previous_marker.clear();
        }
        new NRPlaces.Builder()
                .listener(FragmentPlace.this)
                .key("AIzaSyAOp6FjpfM32AZ2aiobjruHBtxGTu2sNMk")
                .latlng(currentPosition.latitude, currentPosition.longitude)      // ?????? ??????
                .radius(500)                                                      // 500 ?????? ????????? ??????
                .type(PlaceType.LIBRARY)                                          // ?????????
                .build().execute();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Fragement????????? OnCreateView??? ?????????, Activity?????? onCreate()??? ???????????? ?????? ???????????? ???????????????.
        // Activity??? Fragment??? ?????? ?????? ????????? ?????????, View??? ???????????? ????????? ????????? ?????????.
        super.onActivityCreated(savedInstanceState);

        //??????????????? ?????? ????????? ??? ???????????? ??????
        MapsInitializer.initialize(mContext);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // ???????????? ?????????????????? ??????
                .setInterval(UPDATE_INTERVAL_MS) // ????????? Update ?????? ??????
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS); // ?????? ????????? ?????????????????? ??????

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        // FusedLocationProviderClient ?????? ??????
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setDefaultLocation();                       // GPS??? ?????? ????????? ????????? ?????? ?????? ????????? ?????? ????????? ?????????.
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setDefaultLocation() {
        if (currentMarker != null) currentMarker.remove();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15);
        mMap.moveCamera(cameraUpdate);
    }

    String getCurrentAddress(LatLng latlng) {
        // ?????? ????????? ?????????????????? ?????? ???????????? ?????????.
        List<Address> addressList = null ;
        Geocoder geocoder = new Geocoder( mContext, Locale.getDefault());

        // ??????????????? ???????????? ?????? ???????????? ?????????.
        try {
            addressList = geocoder.getFromLocation(latlng.latitude,latlng.longitude,1);
        } catch (IOException e) {
            Toast. makeText( mContext, "??????????????? ????????? ????????? ??? ????????????. ??????????????? ???????????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
            return "?????? ?????? ??????" ;
        }

        if (addressList.size() < 1) { // ?????? ???????????? ?????? ?????????
            return "?????? ????????? ?????? ??????" ;
        }

        // ????????? ?????? ???????????? ???????????? ??????
        Address address = addressList.get(0);
        StringBuilder addressStringBuilder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressStringBuilder.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex())
                addressStringBuilder.append("\n");
        }

        return addressStringBuilder.toString();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                //?????? ????????? ?????? ???????????? ??????
                setCurrentLocation(location);
                mCurrentLocation = location;
            }
        }
    };

    public void setCurrentLocation(Location location) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @Override
    public void onStart() { // ???????????? Fragment??? ???????????? ?????????.
        super.onStart();
        mapView.onStart();
        Log.d(TAG, "onStart ");
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onStop : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onResume() { // ???????????? Fragment??? ????????????, ????????? ??????????????? ???????????? ?????? ??????
        super.onResume();
        mapView.onResume();

        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "onResume : requestLocationUpdates");
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                if (mMap!=null)
                    mMap.setMyLocationEnabled(true);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() { // ?????????????????? ????????? View ??? ???????????? ??????
        super.onDestroyView();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onDestroyView : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        // Destroy ??? ??????, ????????? OnDestroyView?????? View??? ????????????, OnDestroy()??? ????????????.
        super.onDestroy();
        mapView.onDestroy();
    }
}
