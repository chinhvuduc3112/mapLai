package ng.dat.ar;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import ng.dat.ar.helper.DirectionFinderHelper;
import ng.dat.ar.helper.OnDirectionFinderListener;
import ng.dat.ar.model.Direction;

public class DirectionActivity extends AppCompatActivity implements OnDirectionFinderListener {

    private GoogleMap map;
    private SupportMapFragment supportMapFragment;
    private static final String DESTINATION_NAME = "DESTINATION_NAME";
    private static final String CURRENT_LAT = "CURRENT_LAT";
    private static final String CURRENT_LON = "CURRENT_LON";
    private static final String DESTINATION_LAT = "DESTINATION_LAT";
    private static final String DESTINATION_LON = "DESTINATION_LON";

    private LatLng current;
    private LatLng destination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    private String destinationName = "destination";

    private RelativeLayout rlDirectionInfo;
    private TextView tvDistance;
    private TextView tvDuration;
    private Marker currentMarker;
    private Marker destinationMarker;

    public static Intent newInstance(Context context, String destinationName, double currentLat, double currentLon, double destinationLat, double destinationLon) {

        Bundle args = new Bundle();
        args.putString(DESTINATION_NAME, destinationName);
        args.putDouble(CURRENT_LAT, currentLat);
        args.putDouble(CURRENT_LON, currentLon);
        args.putDouble(DESTINATION_LAT, destinationLat);
        args.putDouble(DESTINATION_LON, destinationLon);
        Intent intent = new Intent(context, DirectionActivity.class);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        loadInstance();
        prepareMap();
        addControls();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addControls() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rlDirectionInfo = (RelativeLayout) findViewById(R.id.rlDirectionInfo);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
    }

    private void loadInstance() {
        Bundle bundle = getIntent().getExtras();
        destinationName = bundle.getString(DESTINATION_NAME);
        double currentLat = bundle.getDouble(CURRENT_LAT);
        double currentLon = bundle.getDouble(CURRENT_LON);
        double destinationLat = bundle.getDouble(DESTINATION_LAT);
        double destinationLon = bundle.getDouble(DESTINATION_LON);
        current = new LatLng(currentLat, currentLon);
        destination = new LatLng(destinationLat, destinationLon);
    }

    private void prepareMap() {
        FragmentManager fm = getSupportFragmentManager();
        supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.fmMap);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.fmMap, supportMapFragment).commit();
        }
        if (map == null) {
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 5));

                    currentMarker = map.addMarker(getCurrentMarker());
                    destinationMarker = map.addMarker(getDestinationMarker());
                    new DirectionFinderHelper(getApplicationContext(), DirectionActivity.this, current, destination);
                }
            });
        }
    }

    private MarkerOptions getCurrentMarker() {
        MarkerOptions marker = new MarkerOptions();
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_red_500_24dp));
        marker.title("You're here!");
        marker.position(new LatLng(current.latitude, current.longitude));
        return marker;
    }

    private MarkerOptions getDestinationMarker() {
        MarkerOptions marker = new MarkerOptions();
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_yellow_500_24dp));
        marker.title(destinationName);
        marker.position(new LatLng(destination.latitude, destination.longitude));
        return marker;
    }

    @Override
    public void onDirectionFinderStart() {
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
            rlDirectionInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Direction> directions) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Direction direction : directions) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(direction.startLocation, 10));
            rlDirectionInfo.setVisibility(View.VISIBLE);
            tvDistance.setText(direction.distance.text);
            tvDuration.setText(direction.duration.text);

            originMarkers.add(currentMarker);
            destinationMarkers.add(destinationMarker);

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < direction.points.size(); i++)
                polylineOptions.add(direction.points.get(i));

            polylinePaths.add(map.addPolyline(polylineOptions));
        }
    }
}
