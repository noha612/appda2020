package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        mapView.setTileSource(new XYTileSource(
                "MySource",
                0, 18, 256, ".png",
                new String[]{"http://192.168.43.11:8081/styles/osm-bright/"}
        ));
        mapView.setTilesScaledToDpi(true);
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mapController = mapView.getController();
        mapController.setZoom(18L);
        GeoPoint geoPoint = new GeoPoint(20.981406, 105.787729);
        mapController.setCenter(geoPoint);

        loadRawNode();
        try {

            InputStream inputStream = ctx.getResources().openRawResource(R.raw.edge);

            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line;
            while ((line = buffreader.readLine()) != null) {
                String[] arr = line.split(" ");
                List<GeoPoint> geoPoints = new ArrayList<>();
                geoPoints.add(new GeoPoint(listR.get(arr[0])[0], listR.get(arr[0])[1]));
                geoPoints.add(new GeoPoint(listR.get(arr[1])[0], listR.get(arr[1])[1]));
                s.add(arr[0]);
                s.add(arr[1]);
                Polyline pl = new Polyline();
                pl.getOutlinePaint().setColor(Color.RED);
                pl.setPoints(geoPoints);
                mapView.getOverlayManager().add(pl);
            }
        } catch (IOException e) {
        }
//
//        //original route
//        try {
//            String json = "{ \"from\": { \"lat\": 20.9796429, \"lng\": 105.7894867 }, \"to\": { \"lat\": 20.9793484, \"lng\": 105.8013902 }, \"junctions\": [ { \"lat\": 20.9796429, \"lng\": 105.7894867, \"id\": \"4025216936\" }, { \"lat\": 20.9793246, \"lng\": 105.7898131, \"id\": \"7432990799\" }, { \"lat\": 20.9791618, \"lng\": 105.7899445, \"id\": \"7432990796\" }, { \"lat\": 20.9793271, \"lng\": 105.7901886, \"id\": \"7432990809\" }, { \"lat\": 20.9793046, \"lng\": 105.7902154, \"id\": \"7432990810\" }, { \"lat\": 20.9793046, \"lng\": 105.7902744, \"id\": \"7432990811\" }, { \"lat\": 20.979376, \"lng\": 105.7903911, \"id\": \"6661249715\" }, { \"lat\": 20.9794123, \"lng\": 105.7904943, \"id\": \"6661249716\" }, { \"lat\": 20.9794035, \"lng\": 105.790595, \"id\": \"6661249717\" }, { \"lat\": 20.9793334, \"lng\": 105.7906298, \"id\": \"6661249718\" }, { \"lat\": 20.9792946, \"lng\": 105.7906419, \"id\": \"6661249719\" }, { \"lat\": 20.9792545, \"lng\": 105.7906473, \"id\": \"6661249720\" }, { \"lat\": 20.9791581, \"lng\": 105.790654, \"id\": \"6661249721\" }, { \"lat\": 20.9791209, \"lng\": 105.790705, \"id\": \"445225539\" }, { \"lat\": 20.9779253, \"lng\": 105.7916483, \"id\": \"2471646381\" }, { \"lat\": 20.9774678, \"lng\": 105.7920148, \"id\": \"445225405\" }, { \"lat\": 20.9770379, \"lng\": 105.7923586, \"id\": \"2471646387\" }, { \"lat\": 20.9770123, \"lng\": 105.7924367, \"id\": \"5710669307\" }, { \"lat\": 20.9770371, \"lng\": 105.7925159, \"id\": \"2471646386\" }, { \"lat\": 20.9773297, \"lng\": 105.7929894, \"id\": \"2471646385\" }, { \"lat\": 20.9775153, \"lng\": 105.7940481, \"id\": \"2570970397\" }, { \"lat\": 20.977526, \"lng\": 105.7941065, \"id\": \"5710669303\" }, { \"lat\": 20.9777233, \"lng\": 105.795186, \"id\": \"5710675055\" }, { \"lat\": 20.9773425, \"lng\": 105.7956075, \"id\": \"5710675050\" }, { \"lat\": 20.9771983, \"lng\": 105.7957671, \"id\": \"5710675049\" }, { \"lat\": 20.9775127, \"lng\": 105.7962046, \"id\": \"2291268386\" }, { \"lat\": 20.977566, \"lng\": 105.7962967, \"id\": \"2291268388\" }, { \"lat\": 20.9778934, \"lng\": 105.7967264, \"id\": \"2291268383\" }, { \"lat\": 20.9780455, \"lng\": 105.7969327, \"id\": \"2763229333\" }, { \"lat\": 20.9781697, \"lng\": 105.7970949, \"id\": \"5710675044\" }, { \"lat\": 20.9783504, \"lng\": 105.796941, \"id\": \"6666577664\" }, { \"lat\": 20.9784754, \"lng\": 105.7968345, \"id\": \"6652117939\" }, { \"lat\": 20.9785458, \"lng\": 105.7967746, \"id\": \"1897849719\" }, { \"lat\": 20.9785947, \"lng\": 105.7970615, \"id\": \"6415966270\" }, { \"lat\": 20.9786693, \"lng\": 105.7974993, \"id\": \"6415966271\" }, { \"lat\": 20.9786988, \"lng\": 105.7976723, \"id\": \"6652111325\" }, { \"lat\": 20.9787252, \"lng\": 105.7978267, \"id\": \"6651975136\" }, { \"lat\": 20.9787724, \"lng\": 105.7981035, \"id\": \"6415966261\" }, { \"lat\": 20.9788394, \"lng\": 105.7984969, \"id\": \"6666577666\" }, { \"lat\": 20.9789016, \"lng\": 105.7988616, \"id\": \"5716411035\" }, { \"lat\": 20.9789251, \"lng\": 105.7989638, \"id\": \"4867710521\" }, { \"lat\": 20.9790126, \"lng\": 105.7994592, \"id\": \"1897849717\" }, { \"lat\": 20.9792619, \"lng\": 105.8008928, \"id\": \"5716410790\" }, { \"lat\": 20.9793484, \"lng\": 105.8013902, \"id\": \"1897849721\" } ], \"traffics\": { \"4025216936_7432990799\": 1, \"7432990799_7432990796\": 1, \"7432990796_7432990809\": 1, \"7432990809_7432990810\": 1, \"7432990810_7432990811\": 1, \"7432990811_6661249715\": 1, \"6661249715_6661249716\": 1, \"6661249716_6661249717\": 1, \"6661249717_6661249718\": 1, \"6661249718_6661249719\": 1, \"6661249719_6661249720\": 1, \"6661249720_6661249721\": 1, \"6661249721_445225539\": 1, \"445225539_2471646381\": 1, \"2471646381_445225405\": 1, \"445225405_2471646387\": 1, \"2471646387_5710669307\": 1, \"5710669307_2471646386\": 1, \"2471646386_2471646385\": 1, \"2471646385_2570970397\": 1, \"2570970397_5710669303\": 1, \"5710669303_5710675055\": 1, \"5710675055_5710675050\": 1, \"5710675050_5710675049\": 1, \"5710675049_2291268386\": 1, \"2291268386_2291268388\": 1, \"2291268388_2291268383\": 1, \"2291268383_2763229333\": 1, \"2763229333_5710675044\": 1, \"5710675044_6666577664\": 1, \"6666577664_6652117939\": 1, \"6652117939_1897849719\": 1, \"1897849719_6415966270\": 1, \"6415966270_6415966271\": 1, \"6415966271_6652111325\": 1, \"6652111325_6651975136\": 1, \"6651975136_6415966261\": 1, \"6415966261_6666577666\": 1, \"6666577666_5716411035\": 1, \"5716411035_4867710521\": 1, \"4867710521_1897849717\": 1, \"1897849717_5716410790\": 1, \"5716410790_1897849721\": 1 } }";
//            Direction d = new Gson().fromJson(json, Direction.class);
//
//            List<GeoPoint> route = new ArrayList<>();
//            for (Junction i : d.getJunctions()) {
//                route.add(new GeoPoint(i.getLat(), i.getLng()));
//            }
//
//            Polyline line = new Polyline();
//            line.getOutlinePaint().setColor(Color.BLACK);
//            line.setPoints(route);
//            line.getOutlinePaint().setStrokeWidth(14F);
//            mapView.getOverlayManager().add(line);
//        } catch (Exception e) {
//
//        }
//
//        //congestion
//        try {
//
//            List<GeoPoint> route = new ArrayList<>();
//            route.add(new GeoPoint(20.9779253, 105.7916483));
//            route.add(new GeoPoint(20.9774678, 105.7920148));
//
//            Polyline line = new Polyline();
//            line.getOutlinePaint().setColor(Color.YELLOW);
//            line.setPoints(route);
//            line.getOutlinePaint().setStrokeWidth(14F);
//            mapView.getOverlayManager().add(line);
//        } catch (Exception e) {
//
//        }
//
//        //new route
//        try {
//            String json = "{ \"from\": { \"lat\": 20.9796429, \"lng\": 105.7894867 }, \"to\": { \"lat\": 20.9793484, \"lng\": 105.8013902 }, \"junctions\": [ { \"lat\": 20.9796429, \"lng\": 105.7894867, \"id\": \"4025216936\" }, { \"lat\": 20.9793246, \"lng\": 105.7898131, \"id\": \"7432990799\" }, { \"lat\": 20.9791618, \"lng\": 105.7899445, \"id\": \"7432990796\" }, { \"lat\": 20.9793271, \"lng\": 105.7901886, \"id\": \"7432990809\" }, { \"lat\": 20.9793046, \"lng\": 105.7902154, \"id\": \"7432990810\" }, { \"lat\": 20.9793046, \"lng\": 105.7902744, \"id\": \"7432990811\" }, { \"lat\": 20.979376, \"lng\": 105.7903911, \"id\": \"6661249715\" }, { \"lat\": 20.9794123, \"lng\": 105.7904943, \"id\": \"6661249716\" }, { \"lat\": 20.9794035, \"lng\": 105.790595, \"id\": \"6661249717\" }, { \"lat\": 20.9793334, \"lng\": 105.7906298, \"id\": \"6661249718\" }, { \"lat\": 20.9792946, \"lng\": 105.7906419, \"id\": \"6661249719\" }, { \"lat\": 20.9792545, \"lng\": 105.7906473, \"id\": \"6661249720\" }, { \"lat\": 20.9791581, \"lng\": 105.790654, \"id\": \"6661249721\" }, { \"lat\": 20.9791209, \"lng\": 105.790705, \"id\": \"445225539\" }, { \"lat\": 20.9779253, \"lng\": 105.7916483, \"id\": \"2471646381\" }, { \"lat\": 20.9779902, \"lng\": 105.7917389, \"id\": \"6661249725\" }, { \"lat\": 20.9781346, \"lng\": 105.7919406, \"id\": \"6662689552\" }, { \"lat\": 20.9787517, \"lng\": 105.7928029, \"id\": \"4025242588\" }, { \"lat\": 20.978824, \"lng\": 105.7928971, \"id\": \"4463934560\" }, { \"lat\": 20.9791798, \"lng\": 105.7933579, \"id\": \"2607827425\" }, { \"lat\": 20.9794379, \"lng\": 105.7937076, \"id\": \"2291268390\" }, { \"lat\": 20.9794582, \"lng\": 105.7937365, \"id\": \"6651932982\" }, { \"lat\": 20.9797538, \"lng\": 105.794158, \"id\": \"4463874913\" }, { \"lat\": 20.9797871, \"lng\": 105.7942117, \"id\": \"2291268384\" }, { \"lat\": 20.9798554, \"lng\": 105.7943093, \"id\": \"2291268395\" }, { \"lat\": 20.9801888, \"lng\": 105.7947619, \"id\": \"2291268387\" }, { \"lat\": 20.9803198, \"lng\": 105.7949387, \"id\": \"2763229331\" }, { \"lat\": 20.9798044, \"lng\": 105.7953726, \"id\": \"2763231225\" }, { \"lat\": 20.979095, \"lng\": 105.7959865, \"id\": \"5710675028\" }, { \"lat\": 20.9791522, \"lng\": 105.7960659, \"id\": \"5710675026\" }, { \"lat\": 20.9792338, \"lng\": 105.796165, \"id\": \"5710675027\" }, { \"lat\": 20.9791523, \"lng\": 105.7962508, \"id\": \"5710675039\" }, { \"lat\": 20.979, \"lng\": 105.7963745, \"id\": \"2763232124\" }, { \"lat\": 20.9785458, \"lng\": 105.7967746, \"id\": \"1897849719\" }, { \"lat\": 20.9785947, \"lng\": 105.7970615, \"id\": \"6415966270\" }, { \"lat\": 20.9786693, \"lng\": 105.7974993, \"id\": \"6415966271\" }, { \"lat\": 20.9786988, \"lng\": 105.7976723, \"id\": \"6652111325\" }, { \"lat\": 20.9787252, \"lng\": 105.7978267, \"id\": \"6651975136\" }, { \"lat\": 20.9787724, \"lng\": 105.7981035, \"id\": \"6415966261\" }, { \"lat\": 20.9788394, \"lng\": 105.7984969, \"id\": \"6666577666\" }, { \"lat\": 20.9789016, \"lng\": 105.7988616, \"id\": \"5716411035\" }, { \"lat\": 20.9789251, \"lng\": 105.7989638, \"id\": \"4867710521\" }, { \"lat\": 20.9790126, \"lng\": 105.7994592, \"id\": \"1897849717\" }, { \"lat\": 20.9792619, \"lng\": 105.8008928, \"id\": \"5716410790\" }, { \"lat\": 20.9793484, \"lng\": 105.8013902, \"id\": \"1897849721\" } ], \"traffics\": { \"4025216936_7432990799\": 1, \"7432990799_7432990796\": 1, \"7432990796_7432990809\": 1, \"7432990809_7432990810\": 1, \"7432990810_7432990811\": 1, \"7432990811_6661249715\": 1, \"6661249715_6661249716\": 1, \"6661249716_6661249717\": 1, \"6661249717_6661249718\": 1, \"6661249718_6661249719\": 1, \"6661249719_6661249720\": 1, \"6661249720_6661249721\": 1, \"6661249721_445225539\": 1, \"445225539_2471646381\": 1, \"2471646381_6661249725\": 1, \"6661249725_6662689552\": 1, \"6662689552_4025242588\": 1, \"4025242588_4463934560\": 1, \"4463934560_2607827425\": 1, \"2607827425_2291268390\": 1, \"2291268390_6651932982\": 1, \"6651932982_4463874913\": 1, \"4463874913_2291268384\": 1, \"2291268384_2291268395\": 1, \"2291268395_2291268387\": 1, \"2291268387_2763229331\": 1, \"2763229331_2763231225\": 1, \"2763231225_5710675028\": 1, \"5710675028_5710675026\": 1, \"5710675026_5710675027\": 1, \"5710675027_5710675039\": 1, \"5710675039_2763232124\": 1, \"2763232124_1897849719\": 1, \"1897849719_6415966270\": 1, \"6415966270_6415966271\": 1, \"6415966271_6652111325\": 1, \"6652111325_6651975136\": 1, \"6651975136_6415966261\": 1, \"6415966261_6666577666\": 1, \"6666577666_5716411035\": 1, \"5716411035_4867710521\": 1, \"4867710521_1897849717\": 1, \"1897849717_5716410790\": 1, \"5716410790_1897849721\": 1 } }";
//            Direction d = new Gson().fromJson(json, Direction.class);
//
//            List<GeoPoint> route = new ArrayList<>();
//            for (Junction i : d.getJunctions()) {
//                route.add(new GeoPoint(i.getLat(), i.getLng()));
//            }
//
//            Polyline line = new Polyline();
//            line.getOutlinePaint().setColor(Color.BLUE);
//            line.setPoints(route);
//            line.getOutlinePaint().setStrokeWidth(14F);
//            mapView.getOverlayManager().add(line);
//        } catch (Exception e) {
//
//        }
//
//
//        try {
//            Marker sM = new Marker(mapView);
//            sM.setPosition(new GeoPoint(20.9796429, 105.7894867));
//            sM.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            sM.setTextLabelFontSize(50);
//            sM.setTextIcon("S");
//            mapView.getOverlays().add(sM);
//
//            Marker gM = new Marker(mapView);
//            gM.setPosition(new GeoPoint(20.9793484, 105.8013902));
//            gM.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            gM.setTextLabelFontSize(50);
//            gM.setTextIcon("G");
//            mapView.getOverlays().add(gM);
//        } catch (Exception e) {
//
//        }

        try {
            Marker gps = new Marker(mapView);
            gps.setPosition(new GeoPoint(20.98067, 105.78794));
            gps.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            gps.setTextLabelFontSize(50);
            gps.setTextIcon("(lat, lng)");
            mapView.getOverlays().add(gps);

            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(20.98067, 105.78794));
            geoPoints.add(new GeoPoint(20.9806662, 105.7880563));
            Polyline pl = new Polyline();
            pl.getOutlinePaint().setColor(Color.BLACK);
            pl.setPoints(geoPoints);
            pl.getOutlinePaint().setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
            mapView.getOverlayManager().add(pl);
        } catch (Exception e) {

        }


        for (String i : s) {
            Marker gps = new Marker(mapView);
            gps.setPosition(new GeoPoint(listR.get(i)[0], listR.get(i)[1]));
            gps.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            gps.setTextLabelFontSize(30);
            gps.setTextIcon("X");
            mapView.getOverlays().add(gps);

        }
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