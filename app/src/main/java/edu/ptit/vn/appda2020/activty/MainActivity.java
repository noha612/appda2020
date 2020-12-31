package edu.ptit.vn.appda2020.activty;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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
import edu.ptit.vn.appda2020.util.CommonUtils;

public class MainActivity extends AppCompatActivity {

    //map
    MapView mapView;
    IMapController mapController;
    MyLocationNewOverlay gps;
    RotationGestureOverlay rotation;
    CompassOverlay compass;
    Location from;
    Location to;
    Marker fromMarker;
    Marker toMarker;
    List<GeoPoint> route;
    List<GeoPoint> lstGPWalkFrom;
    List<GeoPoint> lstGPWalkTo;
    Polyline line;
    Polyline walkFrom;
    Polyline walkTo;
    String TAP_CODE = null;
    Gson gson = new Gson();
    APIService mAPIService;
    Button fab;
    int trigger = 0;
    double l;
    double di;

    //directionMode
    ConstraintLayout directionMode;
    CardView mainCard;
    Button findRouteBtn;
    Button miniCardView;
    Button alert;
    Button expandCardView;
    TextView startClick;
    TextView finishClick;
    CardView subCard;
    Button closeSubCard;
    TextView routeInfo;

    //alertMode
    FrameLayout alertMode;
    Button alertBackToMain;
    Button btnLow;
    Button btnMid;
    Button btnHigh;
    LinearLayout alertStep1;
    LinearLayout alertStep2;
    TextView alertGuide;

    //pickMode
    FrameLayout pickMode;
    TextView pickGuide;
    Button exitPickMode;
    Button pick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAPIService = ApiUtils.getAPIService(this);
        setContentView(R.layout.activity_main);
        CommonUtils.setTranslucentStatus(this, true);
        CommonUtils.MIUISetStatusBarLightMode(this, true);

        directionMode = findViewById(R.id.directionMode);
        alertMode = findViewById(R.id.alertMode);
        pickMode = findViewById(R.id.pickMode);

        directionMode.setVisibility(View.VISIBLE);
        alertMode.setVisibility(View.INVISIBLE);
        alertMode.setEnabled(false);
        pickMode.setVisibility(View.INVISIBLE);
        pickMode.setEnabled(false);

        initMap();
        initDirectionMode();
        initAlertMode();
        initPickMode();
    }

    private void initMap() {
//            PTIT
//            GeoPoint geoPoint = new GeoPoint(20.9935828, 105.8061848);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = findViewById(R.id.map);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
//        mapView.setTileSource(new XYTileSource(
//                "MySource",
//                0, 18, 256, ".png",
//                new String[]{"http://192.168.0.107:8081/styles/osm-bright/"}
//        ));
        mapView.setTilesScaledToDpi(true);
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mapController = mapView.getController();
        gps = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        gps.enableMyLocation();
        gps.enableFollowLocation();
        gps.setPersonIcon(CommonUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_baseline_person_pin_24));
        mapView.getOverlays().add(this.gps);
        mapController.animateTo(gps.getMyLocation());
        mapController.setZoom(16L);
        mapView.setMultiTouchControls(true);

        rotation = new RotationGestureOverlay(mapView);
        rotation.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(this.rotation);

        compass = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        compass.enableCompass();
        mapView.getOverlays().add(compass);
        compass.setCompassCenter(350, 480);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (TAP_CODE != null) {
                    onTap(p);
                }

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        mapView.getOverlays().add(OverlayEvents);
    }

    private void initDirectionMode() {
        //direction mode view
        from = new Location();
        to = new Location();
        findRouteBtn = findViewById(R.id.button);
        startClick = findViewById(R.id.startClick);
        finishClick = findViewById(R.id.finishClick);
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
        mainCard = findViewById(R.id.mainCard);
        miniCardView = findViewById(R.id.miniCardView);
        expandCardView = findViewById(R.id.expandCardView);
        expandCardView.setEnabled(false);
        fab = findViewById(R.id.fab);
        alert = findViewById(R.id.alert);
        subCard = findViewById(R.id.subCard);
        subCard.setVisibility(View.INVISIBLE);
        subCard.setEnabled(false);
        closeSubCard = findViewById(R.id.closeSubCard);
        routeInfo = findViewById(R.id.routeInfo);

        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trigger = 1;
//                if (from.getPlace() != null && to.getPlace() != null) {
//                    getRoute(from.getPlace().getId(), to.getPlace().getId());
//                }
                //TODO remove mock data
                String json = "{ \"from\": { \"lat\": 20.9796429, \"lng\": 105.7894867 }, \"to\": { \"lat\": 20.9793484, \"lng\": 105.8013902 }, \"junctions\": [ { \"lat\": 20.9796429, \"lng\": 105.7894867, \"id\": \"4025216936\" }, { \"lat\": 20.9793246, \"lng\": 105.7898131, \"id\": \"7432990799\" }, { \"lat\": 20.9791618, \"lng\": 105.7899445, \"id\": \"7432990796\" }, { \"lat\": 20.9793271, \"lng\": 105.7901886, \"id\": \"7432990809\" }, { \"lat\": 20.9793046, \"lng\": 105.7902154, \"id\": \"7432990810\" }, { \"lat\": 20.9793046, \"lng\": 105.7902744, \"id\": \"7432990811\" }, { \"lat\": 20.979376, \"lng\": 105.7903911, \"id\": \"6661249715\" }, { \"lat\": 20.9794123, \"lng\": 105.7904943, \"id\": \"6661249716\" }, { \"lat\": 20.9794035, \"lng\": 105.790595, \"id\": \"6661249717\" }, { \"lat\": 20.9793334, \"lng\": 105.7906298, \"id\": \"6661249718\" }, { \"lat\": 20.9792946, \"lng\": 105.7906419, \"id\": \"6661249719\" }, { \"lat\": 20.9792545, \"lng\": 105.7906473, \"id\": \"6661249720\" }, { \"lat\": 20.9791581, \"lng\": 105.790654, \"id\": \"6661249721\" }, { \"lat\": 20.9791209, \"lng\": 105.790705, \"id\": \"445225539\" }, { \"lat\": 20.9779253, \"lng\": 105.7916483, \"id\": \"2471646381\" }, { \"lat\": 20.9774678, \"lng\": 105.7920148, \"id\": \"445225405\" }, { \"lat\": 20.9770379, \"lng\": 105.7923586, \"id\": \"2471646387\" }, { \"lat\": 20.9770123, \"lng\": 105.7924367, \"id\": \"5710669307\" }, { \"lat\": 20.9770371, \"lng\": 105.7925159, \"id\": \"2471646386\" }, { \"lat\": 20.9773297, \"lng\": 105.7929894, \"id\": \"2471646385\" }, { \"lat\": 20.9775153, \"lng\": 105.7940481, \"id\": \"2570970397\" }, { \"lat\": 20.977526, \"lng\": 105.7941065, \"id\": \"5710669303\" }, { \"lat\": 20.9777233, \"lng\": 105.795186, \"id\": \"5710675055\" }, { \"lat\": 20.9773425, \"lng\": 105.7956075, \"id\": \"5710675050\" }, { \"lat\": 20.9771983, \"lng\": 105.7957671, \"id\": \"5710675049\" }, { \"lat\": 20.9775127, \"lng\": 105.7962046, \"id\": \"2291268386\" }, { \"lat\": 20.977566, \"lng\": 105.7962967, \"id\": \"2291268388\" }, { \"lat\": 20.9778934, \"lng\": 105.7967264, \"id\": \"2291268383\" }, { \"lat\": 20.9780455, \"lng\": 105.7969327, \"id\": \"2763229333\" }, { \"lat\": 20.9781697, \"lng\": 105.7970949, \"id\": \"5710675044\" }, { \"lat\": 20.9783504, \"lng\": 105.796941, \"id\": \"6666577664\" }, { \"lat\": 20.9784754, \"lng\": 105.7968345, \"id\": \"6652117939\" }, { \"lat\": 20.9785458, \"lng\": 105.7967746, \"id\": \"1897849719\" }, { \"lat\": 20.9785947, \"lng\": 105.7970615, \"id\": \"6415966270\" }, { \"lat\": 20.9786693, \"lng\": 105.7974993, \"id\": \"6415966271\" }, { \"lat\": 20.9786988, \"lng\": 105.7976723, \"id\": \"6652111325\" }, { \"lat\": 20.9787252, \"lng\": 105.7978267, \"id\": \"6651975136\" }, { \"lat\": 20.9787724, \"lng\": 105.7981035, \"id\": \"6415966261\" }, { \"lat\": 20.9788394, \"lng\": 105.7984969, \"id\": \"6666577666\" }, { \"lat\": 20.9789016, \"lng\": 105.7988616, \"id\": \"5716411035\" }, { \"lat\": 20.9789251, \"lng\": 105.7989638, \"id\": \"4867710521\" }, { \"lat\": 20.9790126, \"lng\": 105.7994592, \"id\": \"1897849717\" }, { \"lat\": 20.9792619, \"lng\": 105.8008928, \"id\": \"5716410790\" }, { \"lat\": 20.9793484, \"lng\": 105.8013902, \"id\": \"1897849721\" } ], \"traffics\": { \"4025216936_7432990799\": 1, \"7432990799_7432990796\": 1, \"7432990796_7432990809\": 1, \"7432990809_7432990810\": 1, \"7432990810_7432990811\": 1, \"7432990811_6661249715\": 1, \"6661249715_6661249716\": 1, \"6661249716_6661249717\": 1, \"6661249717_6661249718\": 1, \"6661249718_6661249719\": 1, \"6661249719_6661249720\": 1, \"6661249720_6661249721\": 1, \"6661249721_445225539\": 1, \"445225539_2471646381\": 1, \"2471646381_445225405\": 1, \"445225405_2471646387\": 1, \"2471646387_5710669307\": 1, \"5710669307_2471646386\": 1, \"2471646386_2471646385\": 1, \"2471646385_2570970397\": 1, \"2570970397_5710669303\": 1, \"5710669303_5710675055\": 1, \"5710675055_5710675050\": 1, \"5710675050_5710675049\": 1, \"5710675049_2291268386\": 1, \"2291268386_2291268388\": 1, \"2291268388_2291268383\": 1, \"2291268383_2763229333\": 1, \"2763229333_5710675044\": 1, \"5710675044_6666577664\": 1, \"6666577664_6652117939\": 1, \"6652117939_1897849719\": 1, \"1897849719_6415966270\": 1, \"6415966270_6415966271\": 1, \"6415966271_6652111325\": 1, \"6652111325_6651975136\": 1, \"6651975136_6415966261\": 1, \"6415966261_6666577666\": 1, \"6666577666_5716411035\": 1, \"5716411035_4867710521\": 1, \"4867710521_1897849717\": 1, \"1897849717_5716410790\": 1, \"5716410790_1897849721\": 1 } }";
                Direction d = new Gson().fromJson(json, Direction.class);

                route = new ArrayList<>();
                for (Junction i : d.getJunctions()) {
                    route.add(new GeoPoint(i.getLat(), i.getLng()));
                }
                line.getOutlinePaint().setStrokeJoin(Paint.Join.ROUND);
                line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
                line.getOutlinePaint().setColor(Color.parseColor("#29323c"));
                line.setPoints(route);
                line.getOutlinePaint().setStrokeWidth(14F);
                di = CommonUtils.haversineFomular(
                        new edu.ptit.vn.appda2020.model.dto.GeoPoint(20.9796429, 105.7894867),
                        new edu.ptit.vn.appda2020.model.dto.GeoPoint(20.9793484, 105.8013902)
                );

                mapView.getOverlayManager().add(line);
                mapController.animateTo(route.get(route.size() / 2));
                mapController.setZoom(15.37 * 1.236 / di);

                l = line.getDistance() / 1000;
                l = Math.round(l * 100.0) / 100.0;
                routeInfo.setText(l + " km (18 phút).");

                YoYo.with(Techniques.SlideInUp).duration(250).playOn(subCard);
                subCard.setVisibility(View.VISIBLE);
                subCard.setEnabled(true);
            }
        });

        startClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                intent.putExtra("requestCode", 1);
                startActivityForResult(intent, 1);
            }
        });

        finishClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                intent.putExtra("requestCode", 2);
                startActivityForResult(intent, 2);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.animateTo(gps.getMyLocation());
                mapController.setZoom(17L);
                fab.animate().rotationBy(540).setDuration(500);
            }
        });
        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offDirectionMode();
                onAlertMode();
            }
        });

        miniCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainCard.animate()
                        .translationYBy(-mainCard.getHeight())
                        .alphaBy(-1.0f)
                        .setDuration(250);
                mainCard.setEnabled(!mainCard.isEnabled());

                YoYo.with(Techniques.ZoomIn).duration(250).playOn(expandCardView);
                expandCardView.setEnabled(!expandCardView.isEnabled());

                alert.animate()
                        .translationYBy(-mainCard.getHeight())
                        .setDuration(250);
            }
        });

        expandCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainCard.animate()
                        .translationYBy(mainCard.getHeight())
                        .alphaBy(1.0f)
                        .setDuration(250);
                mainCard.setEnabled(!mainCard.isEnabled());

                YoYo.with(Techniques.ZoomOut).duration(250).playOn(expandCardView);
                expandCardView.setEnabled(!expandCardView.isEnabled());

                alert.animate()
                        .translationYBy(mainCard.getHeight())
                        .setDuration(250);
            }
        });

        closeSubCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trigger = 0;
                hideSubCardAndRoute();
            }
        });
    }

    private void initAlertMode() {
        //alert mode view
        alertBackToMain = findViewById(R.id.alertBackToMain);
        btnLow = findViewById(R.id.btnLow);
        btnMid = findViewById(R.id.btnMid);
        btnHigh = findViewById(R.id.btnHigh);
        alertStep1 = findViewById(R.id.alertStep1);
        alertStep2 = findViewById(R.id.alertStep2);
        alertGuide = findViewById(R.id.alertGuide);

        alertBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offAlertMode();
                onDirectionMode();
            }
        });

        btnLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAlert();
            }
        });

        btnMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAlert();
            }
        });

        btnHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAlert();
            }
        });

    }

    private void initPickMode() {
        //pick mode view
        pickGuide = findViewById(R.id.pickGuide);
        exitPickMode = findViewById(R.id.exitPickMode);
        pick = findViewById(R.id.pick);

        exitPickMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offPickMode();
                String tempTapCode = TAP_CODE;
                onDirectionMode();
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                if (tempTapCode.equalsIgnoreCase("FROM")) {
                    intent.putExtra("requestCode", 1);
                    startActivityForResult(intent, 1);
                } else if (tempTapCode.equalsIgnoreCase("TO")) {
                    intent.putExtra("requestCode", 2);
                    startActivityForResult(intent, 2);
                }
            }
        });

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTap((GeoPoint) mapView.getMapCenter());
                offPickMode();
                onDirectionMode();
            }
        });
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
//                            double roundOff = Math.round(total * 100.0) / 100.0;
//                            final Snackbar snackbar = Snackbar.make(main, roundOff + " km", BaseTransientBottomBar.LENGTH_INDEFINITE);
//                            snackbar.setAction("X", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    snackbar.dismiss();
//                                }
//                            });
//                            snackbar.show();
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
        } else if (resultCode == 1 || resultCode == 2) {
            offDirectionMode();
            onPickMode(resultCode);
        } else if (resultCode == 11 || resultCode == 22) {
            TAP_CODE = requestCode == 11 ? "FROM" : "TO";
            onTap(gps.getMyLocation());
        }
    }

    private void onTap(final GeoPoint gp) {
        Toast.makeText(this, gp.toDoubleString() + " " + TAP_CODE + " " + mapView.getZoomLevelDouble(), Toast.LENGTH_SHORT).show();
        if ("FROM".equalsIgnoreCase(TAP_CODE) || "TO".equalsIgnoreCase(TAP_CODE)) {
//            mAPIService.getLocations(gp.getLatitude() + "", gp.getLongitude() + "").enqueue(new retrofit2.Callback<Location>() {
//                @Override
//                public void onResponse(retrofit2.Call<Location> call, final retrofit2.Response<Location> response) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (TAP_CODE.equals("FROM")) {
//                                from = response.body();
//                                startClick.setText(from.getPlace().getId());
//                                GeoPoint gp = new GeoPoint(from.getMarker().getLat(), from.getMarker().getLng());
//                                fromMarker.setPosition(gp);
//                                fromMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                fromMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
//                                mapView.getOverlays().add(fromMarker);
//                                mapController.setCenter(gp);
//                                mapController.setZoom(18L);
//                            }
//                            if (TAP_CODE.equals("TO")) {
//                                to = response.body();
//                                finishClick.setText(to.getPlace().getId());
//                                GeoPoint gp = new GeoPoint(to.getMarker().getLat(), to.getMarker().getLng());
//                                toMarker.setPosition(gp);
//                                toMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                toMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
//                                mapView.getOverlays().add(toMarker);
//                                mapController.setCenter(gp);
//                                mapController.setZoom(18L);
//                            }
//                            TAP_CODE = null;
//                            mainCard.setVisibility(View.VISIBLE);
//                        }
//                    });
//
//                }
//
//                @Override
//                public void onFailure(retrofit2.Call<Location> call, Throwable t) {
//
//                }
//            });
        } else if ("ALERT".equalsIgnoreCase(TAP_CODE)) {
            mapController.animateTo(gp);
            mapController.setZoom(18L);
            alertGuide.setText("Chọn mức độ tắc nghẽn");
            YoYo.with(Techniques.Bounce).duration(1000).playOn(alertGuide);
            YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnLow);
            YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnHigh);
            YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnMid);
            alertStep2.setVisibility(View.VISIBLE);
            alertStep2.setEnabled(true);
        }
    }

    private String getIMEIDeviceId() {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Log.d("deviceId", deviceId);
        return deviceId;
    }

    private void sendAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Thank you! " + getIMEIDeviceId())
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        offAlertMode();
                        onDirectionMode();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void hideSubCardAndRoute() {
        mapView.getOverlayManager().remove(line);
        YoYo.with(Techniques.SlideOutDown).duration(250).playOn(subCard);
        subCard.setEnabled(false);
    }

    private void showSubCardAndRoute() {
        mapView.getOverlayManager().add(line);
        YoYo.with(Techniques.SlideInUp).duration(250).playOn(subCard);
        subCard.setEnabled(true);
        mapController.animateTo(route.get(route.size() / 2));
        mapController.setZoom(15.37 * 1.236 / di);
    }

    void onDirectionMode() {
        mainCard.animate()
                .translationYBy(mainCard.getHeight())
                .alphaBy(1.0f)
                .setDuration(250);

        expandCardView.animate()
                .translationYBy(expandCardView.getHeight())
                .alphaBy(1.0f)
                .setDuration(250);

        alert.animate()
                .translationYBy(mainCard.getHeight())
                .alphaBy(1.0f)
                .setDuration(250);

        directionMode.setEnabled(true);

        TAP_CODE = null;
        if (trigger == 1) {
            showSubCardAndRoute();
        }
    }

    void offDirectionMode() {
        mainCard.animate()
                .translationYBy(-mainCard.getHeight())
                .alphaBy(-1.0f)
                .setDuration(250);

        expandCardView.animate()
                .translationYBy(-expandCardView.getHeight())
                .alphaBy(-1.0f)
                .setDuration(250);

        alert.animate()
                .translationYBy(-mainCard.getHeight())
                .alphaBy(-1.0f)
                .setDuration(250);

        directionMode.setEnabled(false);
        if (trigger == 1) {
            hideSubCardAndRoute();
        }
    }

    void onAlertMode() {
        mapController.animateTo(gps.getMyLocation());
        mapController.setZoom(18L);
        alertMode.setVisibility(View.VISIBLE);
        alertMode.setEnabled(true);
        alertGuide.setText("Hãy chọn đoạn đường tắc");
        YoYo.with(Techniques.Bounce).duration(1500).playOn(alertGuide);
        alertStep2.setVisibility(View.INVISIBLE);
        alertStep2.setEnabled(false);

        TAP_CODE = "ALERT";
    }

    void offAlertMode() {
        alertMode.setVisibility(View.INVISIBLE);
        alertMode.setEnabled(false);

    }

    void onPickMode(int resultCode) {
        pickMode.setVisibility(View.VISIBLE);
        pickMode.setEnabled(true);
        YoYo.with(Techniques.Bounce).duration(1500).playOn(pickGuide);
        if (resultCode == 1) TAP_CODE = "FROM";
        if (resultCode == 2) TAP_CODE = "TO";
    }

    void offPickMode() {
        pickMode.setVisibility(View.INVISIBLE);
        pickMode.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (directionMode.isEnabled()) {
            if (trigger == 1) {
                trigger = 0;
                hideSubCardAndRoute();
            } else super.onBackPressed();
        }
        if (alertMode.isEnabled()) {
            offAlertMode();
            onDirectionMode();
        }
        if (pickMode.isEnabled()) {
            offPickMode();
            onDirectionMode();
        }
    }
}
//1.236 15.37