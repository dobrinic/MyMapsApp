package zaalgebru.mymapsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

                                //  FragmentActivity
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchBar;
    private ImageButton menuButton;
    private ImageButton layersButton;

    private Marker marker = null;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    // koordinate Zagreb
    private static LatLng coordinatesZagreb = new LatLng(45.812821, 15.977627);

    private final static int INITIAL_ZOOM = 13;
    private final static int DEFAULT_ZOOM = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        layersButton = (ImageButton) findViewById(R.id.layerButton);
        searchBar = (SearchView) findViewById(R.id.searchBar);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setPadding(0, 145, 0, 0);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setTrafficEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        getLastKnownLocation();
        locate();

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String getSearchResult = String.valueOf(searchBar.getQuery());
                LatLng searchResult = getLatLngFromAddress(getSearchResult);

                mMap.addMarker(new MarkerOptions().position(searchResult).title(getAddressFromLatLng(searchResult)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchResult, DEFAULT_ZOOM));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                getAddressFromLatLng(latLng);

                if (marker != null) {
                    marker.setPosition(latLng);
                    marker.setTitle(String.valueOf(R.string.your_location));
                    marker.setSnippet(getAddressFromLatLng(latLng));
                    marker.showInfoWindow();
                } else {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(String.valueOf(R.string.your_location))
                            .snippet(getAddressFromLatLng(latLng)));
                    marker.showInfoWindow();
                }
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        layersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private LatLng getLatLngFromAddress(String address) {
        LatLng addressLatLng;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList.size() > 0) {
            addressLatLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
        } else {
            Toast.makeText(getApplicationContext(), R.string.location_unavailable, Toast.LENGTH_LONG).show();
            addressLatLng = coordinatesZagreb;
        }
        return addressLatLng;
    }

    private String getAddressFromLatLng(LatLng latLng) {
        String address = new String();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            Log.e("getFromLocation", "geocoder ERROR");
        }

        if (addressList.size() > 0) {
            for (int i = 0; i < addressList.get(0).getMaxAddressLineIndex(); i++)
                address += addressList.get(0).getAddressLine(i) + ";  ";
        }
        return address;
    }

    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("checkSelfPermission", "Permission ERROR");

        } else {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

            if (lastKnownLocation != null) {
                double lastLatitude = lastKnownLocation.getLatitude();
                double lastLongitude = lastKnownLocation.getLongitude();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLatitude, lastLongitude), INITIAL_ZOOM));

            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinatesZagreb, 8));
            }
        }
    }

    private void locate() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                LatLng currentLocation = new LatLng(latitude, longitude);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

                if (marker != null){
                    marker.setPosition(currentLocation);
                    marker.setTitle(String.valueOf(R.string.your_location));
                    marker.setSnippet(getAddressFromLatLng(currentLocation));
                    marker.showInfoWindow();
                }else {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title("Vaša trenutna lokacija")
                            .snippet(getAddressFromLatLng(currentLocation)));
                    marker.showInfoWindow();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("checkSelfPermission", "Permission ERROR");

        } else {
            mMap.setMyLocationEnabled(true);

            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);

            locationManager.requestLocationUpdates(provider, 30000, 20, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("checkSelfPermission", "Permission ERROR");
            return;
        }
        locationManager.removeUpdates(locationListener);
    }
}