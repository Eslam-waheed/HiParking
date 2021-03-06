package com.example.hibarking.driver.google_map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hibarking.R;
import com.example.hibarking.data_class.garage_data;
import com.example.hibarking.driver.booking_package.booking_fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider,garage_id=null;
    private FusedLocationProviderClient FusedLocationClient;
    private boolean locationpermassion = false;
    private SupportMapFragment mapFragment;
    private ArrayList<garage_data> latLngArrayList;
    private TextView location_name,location_unit,location_rate,location_price;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&locationpermassion) {
                get_my_location();
            }
           getdata_from_map();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_maps, container, false);
        map_type_method(v);
        search_method(v);
        imagebottom_my_location(v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(callback);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        FusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        checkLocationPermission();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&locationpermassion) {
            alart_permation();
        }
        else
        {
            mapFragment.getMapAsync(callback);
        }
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            locationpermassion=true;
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationpermassion = true;
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationpermassion = true;

                       locationManager.requestLocationUpdates(provider, 400, 1, (LocationListener) getActivity());
                    }

                } else {
                }
                return;
            }

        }
    }
    private void getdeviceLocation()
    {

        try {
            FusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location !=null )
                    {
                        mMap.setMyLocationEnabled(true);
                        LatLng lat = new LatLng(location.getLatitude(),location.getLongitude() );
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lat));
                        mMap.getMinZoomLevel();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 17));
                    }
                }
            });
        }catch (SecurityException e)
        {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void add_location_zoom_in_map(LatLng currentLocation,int zoom,String title)
    {
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation).title(title));
    }
    private void alart_permation()
    {
        new AlertDialog.Builder(getActivity())
                .setTitle("GPS")
                .setMessage("get Gps")
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        locationpermassion=true;
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                         turnGPSOn();
                    }
                })
                .create()
                .show();
    }
    private void get_my_location()
    {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }getdeviceLocation();

    }

    private void turnGPSOn(){
        Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        getActivity().startActivity(intent);
        }
    private void map_type_method(View v)
    {
        ImageView map_type;
        map_type=(ImageView)v.findViewById(R.id.view_map_type);
        map_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_map_type_alart(v);
            }
        });
    }
    private void get_map_type_alart(View v)
    {
        View view=LayoutInflater.from(getContext()).inflate(R.layout.map_type, null);
        RadioGroup radioGroup=view.findViewById(R.id.rediogroup_map_typ);
        radioGroup.check(R.id.normal_map);
        new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme)
                .setTitle("map type")
                .setView(view)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                          int id=   radioGroup.getCheckedRadioButtonId();
                        if(id==R.id.normal_map)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        else if(id==R.id.hybrid_map)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        }
                        else if(id==R.id.satellite_map)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        }
                        else if(id==R.id.terrain_map)
                        {
                            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        }
                    }
                })
                .create()
                .show();
    }
    private void search_method(View v)
    {
        SearchView search=(SearchView) v.findViewById(R.id.search_view);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
               go_to_search_location(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
               // go_to_search_location(s);
                return false;
            }
        });
    }
    private void go_to_search_location(String s)
    {
        List<Address> arr=new ArrayList<>();
        Geocoder geocoder=new Geocoder(getActivity());
        try {
            arr = geocoder.getFromLocationName(s, 1);
        }catch (Exception e)
        {

        }
        if(arr.size()>0)
        {
            Address address=arr.get(0);
          LatLng l=  new LatLng(address.getLatitude(),address.getLongitude());
           // add_location_zoom_in_map(new LatLng(address.getLatitude(),address.getLongitude()),16,address.getAddressLine(0));
            mMap.addMarker(new MarkerOptions().position(l).title(address.getAddressLine(0)));
            //mMap.getMinZoomLevel();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 16));
        }

    }
    private void imagebottom_my_location(View v)
    {
      ImageView imageButton = (ImageView) v.findViewById(R.id.imagebottom_my_location);
      imageButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              get_my_location();
          }
      });
    }
    private void getdata_from_map()
    {
        latLngArrayList = new ArrayList<>();
        latLngArrayList.add(new garage_data("sadny","city",5,-34, 151, 150,"1","4.5"));
        latLngArrayList.add(new garage_data("sadny2","city",15,-31.083332, 150.916672, 150,"2","4"));
        latLngArrayList.add(new garage_data("sadny3","city",12,-32.916668, 151.750000, 150,"3","4.2"));
        latLngArrayList.add(new garage_data("sadny4","city",2,27.470125, 153.021072, 150,"4","4.8"));

        for (int i = 0; i < latLngArrayList.size(); i++) {

            // adding marker to each location on google maps
            LatLng lat=new LatLng(latLngArrayList.get(i).getLatitude(), latLngArrayList.get(i).getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(lat)
                    .title(latLngArrayList.get(i).getName()))
                     ;
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                for (int i = 0; i < latLngArrayList.size(); i++) {
                    if(latLngArrayList.get(i).getLatitude()==marker.getPosition().latitude&&latLngArrayList.get(i).getLongitude()==marker.getPosition().longitude)
                    {
                        garage_id=latLngArrayList.get(i).getId();
                        int book=  get_location_firebase.get_number_of_booking(garage_id);
                        int empty=latLngArrayList.get(i).getUnit_number()-book;
                        Bottom_Sheet_Menu m=new Bottom_Sheet_Menu(garage_id,latLngArrayList.get(i).getName(),empty+" ",
                                latLngArrayList.get(i).getRate(),latLngArrayList.get(i).getPrice()+" ");
                        m.show(getChildFragmentManager(),"MapsFragment");
                    }
                }


                return false;
            }
        });
    }
}
