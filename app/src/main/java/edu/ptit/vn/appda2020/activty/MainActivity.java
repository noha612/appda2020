package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.ptit.vn.appda2020.R;
import edu.ptit.vn.appda2020.model.dto.Direction;
import edu.ptit.vn.appda2020.model.dto.Junction;
import edu.ptit.vn.appda2020.model.dto.Location;
import edu.ptit.vn.appda2020.model.dto.Place;
import edu.ptit.vn.appda2020.retrofit.APIService;
import edu.ptit.vn.appda2020.retrofit.ApiUtils;
import edu.ptit.vn.appda2020.util.LocationFinder;

public class MainActivity extends AppCompatActivity {
    IMapController mapController;
    MapView mapView;
    Button findRouteBtn;
    TextView startClick;
    TextView finishClick;
    Location from;
    Location to;
    Thread thread;
    Marker gps;
    Marker fromMarker;
    Marker toMarker;
    List<GeoPoint> route;
    List<GeoPoint> lstGPWalkFrom;
    List<GeoPoint> lstGPWalkTo;
    Polyline line;
    Polyline walkFrom;
    Polyline walkTo;
    Gson gson = new Gson();
    FloatingActionButton fab;
    View main;
    String TAP_CODE = null;

    View mainTab;
    APIService mAPIService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        from = new Location();
        to = new Location();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.main);

        initMap();
        mAPIService = ApiUtils.getAPIService(this);


        findRouteBtn = findViewById(R.id.button);
        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from != null && to != null) {
                    getRoute(from.getPlace().getId(), to.getPlace().getId());
                }
            }
        });

        startClick = findViewById(R.id.startClick);
        startClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                intent.putExtra("requestCode", 1);
                startActivityForResult(intent, 1);
            }
        });

        finishClick = findViewById(R.id.finishClick);
        finishClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                intent.putExtra("requestCode", 2);
                startActivityForResult(intent, 2);
            }
        });

        gps = new Marker(mapView);
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LocationFinder finder;
                                double longitude = 0.0, latitude = 0.0;
                                finder = new LocationFinder(MainActivity.this, MainActivity.this);
                                if (finder.canGetLocation()) {
                                    latitude = finder.getLatitude();
                                    longitude = finder.getLongitude();
                                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                                    gps.setPosition(geoPoint);
                                    gps.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                    gps.setIcon(getResources().getDrawable(R.drawable.ic_baseline_directions_bike_24));
                                    mapView.getOverlays().add(gps);
                                }
                            }
                        });
                        Thread.sleep(2000);
//                        mapView.getOverlays().remove(current);
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        fromMarker = new Marker(mapView);
        fromMarker.setTextIcon("From");
        toMarker = new Marker(mapView);
        toMarker.setTextIcon("To");
        route = new ArrayList<>();
        lstGPWalkFrom = new ArrayList<>();
        lstGPWalkTo = new ArrayList<>();
        line = new Polyline();
        walkFrom = new Polyline();
        walkTo = new Polyline();

        thread.start();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationFinder finder;
                double longitude = 0.0, latitude = 0.0;
                finder = new LocationFinder(MainActivity.this, MainActivity.this);
                if (finder.canGetLocation()) {
                    latitude = finder.getLatitude();
                    longitude = finder.getLongitude();
                    GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                    mapController.setCenter(geoPoint);
                    mapController.setZoom(19L);
                }
            }
        });

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (TAP_CODE != null)
                    tapToChooseLocation(p);

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };


        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        mapView.getOverlays().add(OverlayEvents);

        mainTab = findViewById(R.id.mainTab);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (!(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission denied to access your location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMap() {

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = findViewById(R.id.map);
//        mapView.setTileSource(new XYTileSource(
//                "MySource",
//                0, 18, 256, ".png",
//                new String[]{"http://192.168.43.11:8081/styles/osm-bright/"}
//        ));
        mapView.setTilesScaledToDpi(true);
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mapController = mapView.getController();
        mapController.setZoom(18L);
        LocationFinder finder;
        double longitude = 0.0, latitude = 0.0;
        finder = new LocationFinder(MainActivity.this, MainActivity.this);
        if (finder.canGetLocation()) {
            latitude = finder.getLatitude();
            longitude = finder.getLongitude();
//            GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            GeoPoint geoPoint = new GeoPoint(20.981406, 105.787729);
            mapController.setCenter(geoPoint);
        }

        double minlat = 20.8710, minlon = 105.6002, maxlat = 21.1761, maxlon = 106.1393;
        List<GeoPoint> geoPoints = new ArrayList<>();

        geoPoints.add(new GeoPoint(minlat, minlon));
        geoPoints.add(new GeoPoint(minlat, maxlon));
        geoPoints.add(new GeoPoint(maxlat, maxlon));
        geoPoints.add(new GeoPoint(maxlat, minlon));
        geoPoints.add(new GeoPoint(minlat, minlon));

        Polyline line = new Polyline();
        line.getOutlinePaint().setColor(Color.RED);
        line.setPoints(geoPoints);
        mapView.getOverlayManager().add(line);

    }


    private void getRoute(String startId, String finishId) {

        mAPIService.getDirections(startId, finishId).enqueue(new retrofit2.Callback<Direction>() {
            @Override
            public void onResponse(retrofit2.Call<Direction> call, retrofit2.Response<Direction> response) {

                if (response.isSuccessful()) {
                    Log.i("TAG", "post submitted to API." + response.toString());


                    final Direction direction = response.body();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.getOverlays().remove(walkFrom);
                            mapView.getOverlays().remove(line);
                            mapView.getOverlays().remove(walkTo);
                            lstGPWalkFrom.clear();
                            route.clear();
                            lstGPWalkTo.clear();

                            //dashed 1
                            lstGPWalkFrom.add(new GeoPoint(from.getMarker().getLat(), from.getMarker().getLng()));
                            lstGPWalkFrom.add(new GeoPoint(from.getH().getLat(), from.getH().getLng()));
                            walkFrom.setPoints(lstGPWalkFrom);
                            walkFrom.getOutlinePaint().setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

                            //route line
                            route.add(new GeoPoint(from.getH().getLat(), from.getH().getLng()));
                            for (Junction i : direction.getJunctions()) {
                                route.add(new GeoPoint(i.getLat(), i.getLng()));
                            }
                            route.add(new GeoPoint(to.getH().getLat(), to.getH().getLng()));

                            //checking...


                            line.getOutlinePaint().setColor(Color.BLACK);
                            line.setPoints(route);
                            line.getOutlinePaint().setStrokeWidth(6F);

                            //dashed 2
                            lstGPWalkTo.add(new GeoPoint(to.getMarker().getLat(), to.getMarker().getLng()));
                            lstGPWalkTo.add(new GeoPoint(to.getH().getLat(), to.getH().getLng()));
                            walkTo.setPoints(lstGPWalkTo);
                            walkTo.getOutlinePaint().setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

                            //draw
                            mapView.getOverlayManager().add(walkFrom);
                            mapView.getOverlayManager().add(line);
                            mapView.getOverlayManager().add(walkTo);

                            mapController.setCenter(route.get(route.size() / 2));
                            mapController.setZoom(16L);

                            //show distance
                            double total = 0;
//                        for (int i = 1; i < route.size() - 2; i++) {
//                            total += HaversineScorer.computeCost(direction.getRoute().get(i), direction.getRoute().get(i + 1));
//                        }
                            double roundOff = Math.round(total * 100.0) / 100.0;
                            final Snackbar snackbar = Snackbar.make(main, roundOff + " km", BaseTransientBottomBar.LENGTH_INDEFINITE);
                            snackbar.setAction("X", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    });
                }

            }

            @Override
            public void onFailure(retrofit2.Call<Direction> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Place place = (Place) data.getSerializableExtra("location");
            SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
            String his = sharedPreferences.getString("his", null);
            Set<Place> listHis;
            Type type = new TypeToken<Set<Place>>() {
            }.getType();
            if (his != null) {
                listHis = gson.fromJson(his, type);
            } else {
                listHis = new LinkedHashSet<>();
            }
            for (Place i : listHis) {
                if (i.getName().equalsIgnoreCase(place.getName())) {
                    listHis.remove(i);
                    break;
                }
            }
            listHis.add(place);
//            if (listHis.size() > 10) listHis.remove(0);
            sharedPreferences.edit().putString("his", gson.toJson(listHis)).apply();
            if (requestCode == 1) {
                mapView.getOverlays().remove(line);
                from.setPlace(place);
                from.setMarker(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                from.setH(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                startClick.setText(place.getName());
                GeoPoint gp = new GeoPoint(place.getLat(), place.getLng());
                fromMarker.setTitle(place.getName());
                fromMarker.setPosition(gp);
                fromMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                fromMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
                mapView.getOverlays().add(fromMarker);
                mapController.setCenter(gp);
                mapController.setZoom(18L);
            }
            if (requestCode == 2) {
                mapView.getOverlays().remove(line);
                to.setPlace(place);
                to.setMarker(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                to.setH(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                finishClick.setText(place.getName());
                GeoPoint gp = new GeoPoint(place.getLat(), place.getLng());
                toMarker.setTitle(place.getName());
                toMarker.setPosition(gp);
                toMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                toMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
                mapView.getOverlays().add(toMarker);
                mapController.setCenter(gp);
                mapController.setZoom(18L);
            }
        } else {
            mainTab.setVisibility(View.GONE);
            if (resultCode == 1) TAP_CODE = "FROM";
            if (resultCode == 2) TAP_CODE = "TO";
        }
    }

    private void tapToChooseLocation(final GeoPoint gp) {

        mAPIService.getLocations(gp.getLatitude() + "", gp.getLongitude() + "").enqueue(new retrofit2.Callback<Location>() {
            @Override
            public void onResponse(retrofit2.Call<Location> call, final retrofit2.Response<Location> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TAP_CODE.equals("FROM")) {
                            from = response.body();
                            startClick.setText(from.getPlace().getId());
                            GeoPoint gp = new GeoPoint(from.getMarker().getLat(), from.getMarker().getLng());
                            fromMarker.setPosition(gp);
                            fromMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            fromMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
                            mapView.getOverlays().add(fromMarker);
                            mapController.setCenter(gp);
                            mapController.setZoom(18L);
                        }
                        if (TAP_CODE.equals("TO")) {
                            to = response.body();
                            finishClick.setText(to.getPlace().getId());
                            GeoPoint gp = new GeoPoint(to.getMarker().getLat(), to.getMarker().getLng());
                            toMarker.setPosition(gp);
                            toMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            toMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
                            mapView.getOverlays().add(toMarker);
                            mapController.setCenter(gp);
                            mapController.setZoom(18L);
                        }
                        TAP_CODE = null;
                        mainTab.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onFailure(retrofit2.Call<Location> call, Throwable t) {

            }
        });
    }

}