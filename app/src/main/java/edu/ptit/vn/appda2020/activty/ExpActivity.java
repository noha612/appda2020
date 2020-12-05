package edu.ptit.vn.appda2020.activty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import edu.ptit.vn.appda2020.R;

public class ExpActivity extends AppCompatActivity {
    private static Map<String, Double[]> listR;
    private static Set<String> s = new HashSet<>();
    IMapController mapController;
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp);

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
        GeoPoint geoPoint = new GeoPoint(20.981406, 105.787729);
        mapController.setCenter(geoPoint);


//        loadRawNode();
//
//        InputStream inputStream = ctx.getResources().openRawResource(R.raw.edge);
//
//        InputStreamReader inputreader = new InputStreamReader(inputStream);
//        BufferedReader buffreader = new BufferedReader(inputreader);
//        String line;
//        try {
//            while ((line = buffreader.readLine()) != null) {
//                String[] arr = line.split(" ");
//                List<GeoPoint> geoPoints = new ArrayList<>();
//                geoPoints.add(new GeoPoint(listR.get(arr[0])[0], listR.get(arr[0])[1]));
//                geoPoints.add(new GeoPoint(listR.get(arr[1])[0], listR.get(arr[1])[1]));
//                s.add(arr[0]);
//                s.add(arr[1]);
//                Polyline pl = new Polyline();
//                pl.getOutlinePaint().setColor(Color.RED);
//                pl.setPoints(geoPoints);
//                mapView.getOverlayManager().add(pl);
//            }
//        } catch (IOException e) {
//        }
//        for(String i : s){
//            Marker gps = new Marker(mapView);
//            gps.setPosition(new GeoPoint(listR.get(i)[0], listR.get(i)[1]));
//            gps.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            gps.setTextLabelFontSize(30);
//            gps.setTextIcon("X");
//            mapView.getOverlays().add(gps);
//
//        }
    }

    private void loadRawNode() {

        listR = new LinkedHashMap<>();
        Context ctx = getApplicationContext();
        InputStream inputStream = ctx.getResources().openRawResource(R.raw.raw_node);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        try {
            while ((line = buffreader.readLine()) != null) {
                if (StringUtils.isNotEmpty(line)) {
                    String[] temp = line.split(" ");
                    String key = temp[0];
                    Double[] array = new Double[2];
                    for (int i = 0; i < array.length; i++)
                        array[i] = Double.parseDouble(temp[i + 1]);
                    listR.put(key, array);
                    Log.v("R", listR.size() + "");
                }
            }
        } catch (IOException e) {
        }

    }
}