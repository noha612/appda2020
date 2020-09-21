package edu.ptit.vn.appda2020;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView = null;
    IMapController mapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
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

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRoute();
            }
        });

        final EditText start = findViewById(R.id.start);
        final EditText finish = findViewById(R.id.finish);

        start.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getId(start);
                    return true;
                }
                return false;
            }
        });

        finish.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getId(finish);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission denied to access your location.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    String host = "https://6893599359e4.ngrok.io";
    OkHttpClient client = new OkHttpClient();

    private void getId(final EditText edt) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(host + "/da2020/v1/findStation").newBuilder();
        httpBuilder.addQueryParameter("name", edt.getText().toString());
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
                        edt.setText(json);
                        Marker startMarker = new Marker(mapView);
                        startMarker.setPosition(new GeoPoint(20.9878278, 105.7963234));
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.getOverlays().add(startMarker);
                        Toast.makeText(MainActivity.this, json, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void getRoute() {
//        RequestBody body;
//                    body = RequestBody.create(
//                    MediaType.parse("application/json"),
//                    new ObjectMapper().writeValueAsString(new User(username, password))
//            );
//            Request request = new Request.Builder().post(body)
//                    .url(host + "/da2020/v1/findStation?name=ao")
//                    .build();
        HttpUrl.Builder httpBuilder = HttpUrl.parse(host + "/da2020/v1/findRoute").newBuilder();
        httpBuilder.addQueryParameter("startId", "6662689543");
        httpBuilder.addQueryParameter("finishId", "2291276208");
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
                            line.getOutlinePaint().setColor(Color.BLACK);
                            line.setPoints(geoPoints);
                            mapView.getOverlayManager().add(line);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}