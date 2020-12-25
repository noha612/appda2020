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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    //directionMode
    ConstraintLayout directionMode;
    CardView mainCard;
    Button findRouteBtn;
    Button miniCardView;
    Button alert;
    CardView expandCardView;
    TextView startClick;
    TextView finishClick;
    Button fab;

    //alertMode
    FrameLayout alertMode;
    Button alertBackToMain;
    Button btnLow;
    Button btnMid;
    Button btnHigh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAPIService = ApiUtils.getAPIService(this);
        setContentView(R.layout.activity_main);
        directionMode = findViewById(R.id.directionMode);
        alertMode = findViewById(R.id.alertMode);
        initMap();
        initDirectionMode();
        initAlertMode();
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
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
//        mapView.setTileSource(new XYTileSource(
//                "MySource",
//                0, 18, 256, ".png",
//                new String[]{"http://192.168.43.11:8081/styles/osm-bright/"}
//        ));
        mapView.setTilesScaledToDpi(true);
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mapController = mapView.getController();
        mapController.setZoom(16L);
//            PTIT
//            GeoPoint geoPoint = new GeoPoint(20.9935828, 105.8061848);

        mapView.setMultiTouchControls(true);
        gps = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        gps.enableMyLocation();
        gps.enableFollowLocation();
        gps.setPersonIcon(CommonUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_baseline_person_pin_24));
        mapView.getOverlays().add(this.gps);
        mapController.animateTo(gps.getMyLocation());

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


        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from != null && to != null) {
                    getRoute(from.getPlace().getId(), to.getPlace().getId());
                }
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
                        .setDuration(200);
                mainCard.setEnabled(!mainCard.isEnabled());

                expandCardView.animate().rotationBy(-180)
                        .alphaBy(1.0f)
                        .setDuration(200);
                expandCardView.setEnabled(!expandCardView.isEnabled());

                alert.animate()
                        .translationYBy(-mainCard.getHeight())
                        .setDuration(200);
            }
        });

        expandCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainCard.animate()
                        .translationYBy(mainCard.getHeight())
                        .alphaBy(1.0f)
                        .setDuration(200);
                mainCard.setEnabled(!mainCard.isEnabled());

                expandCardView.animate().rotationBy(180)
                        .alphaBy(-1.0f)
                        .setDuration(200);
                expandCardView.setEnabled(!expandCardView.isEnabled());

                alert.animate()
                        .translationYBy(mainCard.getHeight())
                        .setDuration(200);
            }
        });

    }

    private void initAlertMode() {
        //alert mode view
        alertBackToMain = findViewById(R.id.alertBackToMain);
        btnLow = findViewById(R.id.btnLow);
        btnMid = findViewById(R.id.btnMid);
        btnHigh = findViewById(R.id.btnHigh);

        alertBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offAlertMode();
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
            mainCard.setVisibility(View.GONE);
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
                        mainCard.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onFailure(retrofit2.Call<Location> call, Throwable t) {

            }
        });
    }

    void onDirectionMode() {
        mainCard.animate()
                .translationYBy(mainCard.getHeight())
                .alphaBy(1.0f)
                .setDuration(200);

        expandCardView.animate()
                .translationYBy(expandCardView.getHeight())
                .alphaBy(1.0f)
                .setDuration(200);

        alert.animate()
                .translationYBy(mainCard.getHeight())
                .alphaBy(1.0f)
                .setDuration(200);

        directionMode.setEnabled(true);
    }

    void offDirectionMode() {
        mainCard.animate()
                .translationYBy(-mainCard.getHeight())
                .alphaBy(-1.0f)
                .setDuration(200);

        expandCardView.animate()
                .translationYBy(-expandCardView.getHeight())
                .alphaBy(-1.0f)
                .setDuration(200);

        alert.animate()
                .translationYBy(-mainCard.getHeight())
                .alphaBy(-1.0f)
                .setDuration(200);

        directionMode.setEnabled(false);
    }

    void onAlertMode() {
        mapController.animateTo(gps.getMyLocation());
        mapController.setZoom(18L);
        alertMode.setVisibility(View.VISIBLE);
        alertMode.setEnabled(true);
        int h = btnLow.getHeight() + btnMid.getHeight() + btnHigh.getHeight();
        btnLow.animate().translationYBy(btnLow.getHeight());
        btnMid.animate().translationYBy(btnMid.getHeight());
        btnHigh.animate().translationYBy(btnHigh.getHeight());
        btnLow.animate().translationYBy(-btnLow.getHeight()).setDuration(200);
        btnMid.animate().translationYBy(-btnMid.getHeight()).setDuration(200);
        btnHigh.animate().translationYBy(-btnHigh.getHeight()).setDuration(200);
    }

    void offAlertMode() {
        alertMode.setVisibility(View.GONE);
        alertMode.setEnabled(false);
//        btnLow.animate().translationYBy(-h).setDuration(200);
//        btnMid.animate().translationYBy(-h).setDuration(200);
//        btnHigh.animate().translationYBy(-h).setDuration(200);
    }

}