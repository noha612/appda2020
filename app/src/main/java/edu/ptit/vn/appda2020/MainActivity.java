package edu.ptit.vn.appda2020;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    IMapController mapController;
    String host = "https://8e8658761c20.ngrok.io";
    OkHttpClient client = new OkHttpClient();
    String startId;
    String finishId;
    private MapView mapView = null;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        mapView = findViewById(R.id.map);
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

        final AutoCompleteTextView start = findViewById(R.id.start);
        String[] languages = {"Java ", "CSharp", "Visual Basic"};
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        start.setThreshold(2);
        start.setAdapter(autoSuggestAdapter);
        final EditText finish = findViewById(R.id.finish);

        start.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    start.clearFocus();
                    InputMethodManager in = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(start.getWindowToken(), 0);
                    getId(start, "start");
                    return true;
                }
                return false;
            }
        });

        start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(start.getText())) {
                        String[] stringList = {"Java ", "Swift", "Visual Basic"};
                        autoSuggestAdapter.setData(Arrays.asList(stringList));
                        autoSuggestAdapter.notifyDataSetChanged();
                    }
                }
                return false;
            }
        });

        finish.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    finish.clearFocus();
                    InputMethodManager in = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(start.getWindowToken(), 0);
                    getId(finish, "finish");
                    return true;
                }
                return false;
            }
        });


         tv = findViewById(R.id.startClick);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, FindLocationActivity.class),1);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    private void getId(final EditText edt, final String idType) {
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
                final Location location = new ObjectMapper().readValue(json, Location.class);
                if (idType.equals("start")) startId = location.getIntersection().getId();
                else finishId = location.getIntersection().getId();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edt.setText(location.getName());
                        GeoPoint gp = new GeoPoint(location.getIntersection().getLatitude(), location.getIntersection().getLongitude());
                        Marker startMarker = new Marker(mapView);
                        startMarker.setPosition(gp);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.getOverlays().add(startMarker);
                        mapController.setCenter(gp);
                        mapController.setZoom(18L);
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
        if(requestCode==1){
            tv.setText(data.getStringExtra("s"));
        }
    }
}