package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.ptit.vn.appda2020.R;
import edu.ptit.vn.appda2020.model.Intersection;
import edu.ptit.vn.appda2020.model.Location;
import edu.ptit.vn.appda2020.module.LocationFinder;
import edu.ptit.vn.appda2020.util.HaversineScorer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    IMapController mapController;
    OkHttpClient client = new OkHttpClient();
    MapView mapView;
    Button findRouteBtn;
    TextView startClick;
    TextView finishClick;
    Location startLocation;
    Location finishLocation;
    Thread thread;
    Marker current;
    Marker startMarker;
    Marker finishMarker;
    List<GeoPoint> route;
    Polyline line;
    Gson gson = new Gson();
    FloatingActionButton fab;
    View main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.main);

        initMap();

        findRouteBtn = findViewById(R.id.button);
        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startLocation != null && finishLocation != null) {
                    getRoute(startLocation.getIntersection().getId(), finishLocation.getIntersection().getId());
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

        current = new Marker(mapView);
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
                                    current.setPosition(geoPoint);
                                    current.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                    current.setIcon(getResources().getDrawable(R.drawable.ic_baseline_directions_bike_24));
                                    mapView.getOverlays().add(current);
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

        startMarker = new Marker(mapView);
        finishMarker = new Marker(mapView);
        route = new ArrayList<>();
        line = new Polyline();

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
        mapView.setTileSource(TileSourceFactory.HIKEBIKEMAP);
        mapController = mapView.getController();
        mapController.setZoom(16L);
        LocationFinder finder;
        double longitude = 0.0, latitude = 0.0;
        finder = new LocationFinder(MainActivity.this, MainActivity.this);
        if (finder.canGetLocation()) {
            latitude = finder.getLatitude();
            longitude = finder.getLongitude();
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            mapController.setCenter(geoPoint);
        }

        double minlat = 20.9677000, minlon = 105.7714000, maxlat = 20.9944000, maxlon = 105.8250000;
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
        HttpUrl.Builder httpBuilder = HttpUrl.parse(getString(R.string.server_uri) + getString(R.string.api_route)).newBuilder();
        httpBuilder.addQueryParameter("startId", startId);
        httpBuilder.addQueryParameter("finishId", finishId);
        Request request = new Request.Builder().get()
                .url(httpBuilder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Network Error" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String json = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mapView.getOverlays().remove(line);
                            Intersection[] intersections = new ObjectMapper().readValue(json, Intersection[].class);
                            route.clear();
                            for (Intersection i : intersections) {
                                route.add(new GeoPoint(i.getLatitude(), i.getLongitude()));
                            }
                            line.getOutlinePaint().setColor(Color.BLACK);
                            line.setPoints(route);
                            line.getOutlinePaint().setStrokeWidth(6F);
                            mapView.getOverlayManager().add(line);
                            mapController.setCenter(route.get(route.size() / 2));
                            mapController.setZoom(16L);

                            //show distance
                            double total = 0;
                            for (int i = 0; i < intersections.length - 1; i++) {
                                total += HaversineScorer.computeCost(intersections[i], intersections[i + 1]);
                            }
                            double roundOff = Math.round(total * 100.0) / 100.0;
                            final Snackbar snackbar = Snackbar.make(main, roundOff + " km", BaseTransientBottomBar.LENGTH_INDEFINITE);
                            snackbar.setAction("X", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Location location = (Location) data.getSerializableExtra("location");
            SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
            String his = sharedPreferences.getString("his", null);
            Set<Location> listHis;
            Type type = new TypeToken<Set<Location>>() {
            }.getType();
            if (his != null) {
                listHis = gson.fromJson(his, type);
            } else {
                listHis = new LinkedHashSet<>();
            }
            for (Location i : listHis) {
                if (i.getName().equalsIgnoreCase(location.getName())) {
                    listHis.remove(i);
                    break;
                }
            }
            listHis.add(location);
//            if (listHis.size() > 10) listHis.remove(0);
            sharedPreferences.edit().putString("his", gson.toJson(listHis)).apply();
            if (requestCode == 1) {
                mapView.getOverlays().remove(line);
                startLocation = location;
                startClick.setText(startLocation.getName());
                GeoPoint gp = new GeoPoint(location.getIntersection().getLatitude(), location.getIntersection().getLongitude());
                startMarker.setTitle(startLocation.getName());
                startMarker.setPosition(gp);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
                mapView.getOverlays().add(startMarker);
                mapController.setCenter(gp);
                mapController.setZoom(18L);
            }
            if (requestCode == 2) {
                mapView.getOverlays().remove(line);
                finishLocation = location;
                finishClick.setText(finishLocation.getName());
                GeoPoint gp = new GeoPoint(location.getIntersection().getLatitude(), location.getIntersection().getLongitude());
                finishMarker.setTitle(finishLocation.getName());
                finishMarker.setPosition(gp);
                finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                finishMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
                mapView.getOverlays().add(finishMarker);
                mapController.setCenter(gp);
                mapController.setZoom(18L);
            }
        }
    }
}