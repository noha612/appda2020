package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.content.Intent;
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

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ptit.vn.appda2020.model.Intersection;
import edu.ptit.vn.appda2020.model.Location;
import edu.ptit.vn.appda2020.R;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        GeoPoint startPoint = new GeoPoint(20.9878278, 105.7963234);
        mapController.setCenter(startPoint);

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
                            Intersection[] intersections = new ObjectMapper().readValue(json, Intersection[].class);
                            List<GeoPoint> geoPoints = new ArrayList<>();
                            for (Intersection i : intersections) {
                                geoPoints.add(new GeoPoint(i.getLatitude(), i.getLongitude()));
                            }
                            Polyline line = new Polyline();
                            line.getOutlinePaint().setColor(Color.RED);
                            line.setPoints(geoPoints);
                            line.getOutlinePaint().setStrokeWidth(2.5F);
                            mapView.getOverlayManager().clear();
                            mapView.getOverlayManager().add(line);
                            Marker startMarker = new Marker(mapView);

                            startMarker.setPosition(geoPoints.get(0));
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            mapView.getOverlays().add(startMarker);
                            Marker startMarker2 = new Marker(mapView);
                            startMarker2.setPosition(geoPoints.get(geoPoints.size() - 1));
                            startMarker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            mapView.getOverlays().add(startMarker2);
                            mapController.setCenter(geoPoints.get(0));
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
            if (requestCode == 1) {
                startLocation = location;
                startClick.setText(startLocation.getName());
//            GeoPoint gp = new GeoPoint(location.getIntersection().getLatitude(), location.getIntersection().getLongitude());
//            Marker startMarker = new Marker(mapView);
//            startMarker.setPosition(gp);
//            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            mapView.getOverlays().add(startMarker);
//            mapController.setCenter(gp);
//            mapController.setZoom(18L);
            }
            if (requestCode == 2) {
                finishLocation = location;
                finishClick.setText(finishLocation.getName());
            }
        }
    }
}