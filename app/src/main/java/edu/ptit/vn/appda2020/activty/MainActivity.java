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
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
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
import edu.ptit.vn.appda2020.model.dto.AlertDTO;
import edu.ptit.vn.appda2020.model.dto.Direction;
import edu.ptit.vn.appda2020.model.dto.Junction;
import edu.ptit.vn.appda2020.model.dto.Location;
import edu.ptit.vn.appda2020.model.dto.Place;
import edu.ptit.vn.appda2020.model.dto.Road;
import edu.ptit.vn.appda2020.retrofit.APIService;
import edu.ptit.vn.appda2020.retrofit.ApiUtils;
import edu.ptit.vn.appda2020.util.CommonUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    int m;

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
    Button track;
    boolean isTracking = false;

    //alertMode
    FrameLayout alertMode;
    Button alertBackToMain;
    Button btnLow;
    Button btnMid;
    Button btnHigh;
    LinearLayout alertStep1;
    LinearLayout alertStep2;
    TextView alertGuide;
    Road congestRoad;
    List<GeoPoint> lstCongest;
    Polyline lineCongest;

    //pickMode
    FrameLayout pickMode;
    TextView pickGuide;
    Button exitPickMode;
    Button pick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
        compass.setCompassCenter(350, 420);
//        compass.setCompassCenter(350, 480);

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
        track = findViewById(R.id.track);

        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trigger = 1;
                if (from.getPlace() != null && to.getPlace() != null) {
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

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    track.setText("Bắt đầu");
                    gps.disableFollowLocation();
                    isTracking = false;
                } else {
                    track.setText("Tạm dừng");
                    mapController.setZoom(17.5);
                    gps.enableFollowLocation();
                    isTracking = true;
                }
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

        lstCongest = new ArrayList<>();
        lineCongest = new Polyline();

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
                sendAlert(2);
            }
        });

        btnMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAlert(3);
            }
        });

        btnHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAlert(4);
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
                            if (CommonUtils.isStraight(direction.getJunctions().get(0), new edu.ptit.vn.appda2020.model.dto.GeoPoint(from.getH().getLat(), from.getH().getLng()), direction.getJunctions().get(1)))
                                direction.getJunctions().remove(0);
                            int n = direction.getJunctions().size();
                            if (CommonUtils.isStraight(direction.getJunctions().get(n - 1), new edu.ptit.vn.appda2020.model.dto.GeoPoint(to.getH().getLat(), to.getH().getLng()), direction.getJunctions().get(n - 2)))
                                direction.getJunctions().remove(n - 1);
                            route.add(new GeoPoint(from.getH().getLat(), from.getH().getLng()));
                            for (Junction i : direction.getJunctions()) {
                                route.add(new GeoPoint(i.getLat(), i.getLng()));
                            }
                            route.add(new GeoPoint(to.getH().getLat(), to.getH().getLng()));

                            line.getOutlinePaint().setColor(Color.parseColor("#E6203A43"));
                            line.setPoints(route);
                            line.getOutlinePaint().setStrokeWidth(29F);
                            line.getOutlinePaint().setStrokeJoin(Paint.Join.ROUND);
                            line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);

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
                            mapController.zoomTo(13);

                            l = line.getDistance() / 1000;
                            l = Math.round(l * 10.0) / 10.0;
                            m = (int) Math.ceil(l * 3);
                            routeInfo.setText(l + " km (" + m + " phút).");

                            YoYo.with(Techniques.SlideInUp).duration(250).playOn(subCard);
                            subCard.setVisibility(View.VISIBLE);
                            subCard.setEnabled(true);
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
                String displayName = place.getName().length() < 26 ? place.getName() : place.getName().substring(0, 26) + "...";
                startClick.setText(displayName);
                GeoPoint gp = new GeoPoint(place.getLat(), place.getLng());
                fromMarker.setTitle(place.getName());
                fromMarker.setPosition(gp);
                fromMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                fromMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
                mapView.getOverlays().add(fromMarker);
                mapController.animateTo(gp);
                mapController.setZoom(18L);
            }
            if (requestCode == 2) {
                mapView.getOverlays().remove(line);
                to.setPlace(place);
                to.setMarker(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                to.setH(new edu.ptit.vn.appda2020.model.dto.GeoPoint(place.getLat(), place.getLng()));
                String displayName = place.getName().length() < 26 ? place.getName() : place.getName().substring(0, 26) + "...";
                finishClick.setText(displayName);
                GeoPoint gp = new GeoPoint(place.getLat(), place.getLng());
                toMarker.setTitle(place.getName());
                toMarker.setPosition(gp);
                toMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                toMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
                mapView.getOverlays().add(toMarker);
                mapController.animateTo(gp);
                mapController.setZoom(18L);
            }
        } else if (resultCode == 1 || resultCode == 2) {
            offDirectionMode();
            onPickMode(resultCode);
        } else if (resultCode == 11 || resultCode == 22) {
            TAP_CODE = resultCode == 11 ? "FROM" : "TO";
            onTap(gps.getMyLocation());
        }
    }

    private void onTap(final GeoPoint gp) {
//        Toast.makeText(this, gp.toDoubleString() + " " + TAP_CODE + " " + mapView.getZoomLevelDouble(), Toast.LENGTH_SHORT).show();
        if ("FROM".equalsIgnoreCase(TAP_CODE) || "TO".equalsIgnoreCase(TAP_CODE)) {
            mAPIService.getLocations(gp.getLatitude() + "", gp.getLongitude() + "").enqueue(new retrofit2.Callback<Location>() {
                @Override
                public void onResponse(retrofit2.Call<Location> call, final retrofit2.Response<Location> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (TAP_CODE.equals("FROM")) {
                                from = response.body();
                                Place place = from.getPlace();
                                String displayName = place.getName().length() < 26 ? place.getName() : place.getName().substring(0, 26) + "...";
                                startClick.setText(displayName);
                                fromMarker.setTitle(place.getName());
                                GeoPoint gp = new GeoPoint(from.getMarker().getLat(), from.getMarker().getLng());
                                fromMarker.setPosition(gp);
                                fromMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                fromMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_location_on_24));
                                mapView.getOverlays().add(fromMarker);
                                mapController.animateTo(gp);
                                mapController.setZoom(18L);
                            }
                            if (TAP_CODE.equals("TO")) {
                                to = response.body();
                                Place place = to.getPlace();
                                String displayName = place.getName().length() < 26 ? place.getName() : place.getName().substring(0, 26) + "...";
                                finishClick.setText(displayName);
                                toMarker.setTitle(place.getName());
                                GeoPoint gp = new GeoPoint(to.getMarker().getLat(), to.getMarker().getLng());
                                toMarker.setPosition(gp);
                                toMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                toMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_where_to_vote_24));
                                mapView.getOverlays().add(toMarker);
                                mapController.animateTo(gp);
                                mapController.setZoom(18L);
                            }
                            trigger = 0;
                            TAP_CODE = null;
                            mainCard.setVisibility(View.VISIBLE);
                        }
                    });

                }

                @Override
                public void onFailure(retrofit2.Call<Location> call, Throwable t) {

                }
            });
        } else if ("ALERT".equalsIgnoreCase(TAP_CODE)) {

            mAPIService.getRoad(gp.getLatitude() + "", gp.getLongitude() + "").enqueue(new retrofit2.Callback<Road>() {
                @Override
                public void onResponse(retrofit2.Call<Road> call, final retrofit2.Response<Road> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.body() != null && response.body().getId() != null) {
                                congestRoad = response.body();
                                lstCongest.clear();
                                mapView.getOverlays().remove(lineCongest);

                                lstCongest.add(new GeoPoint(congestRoad.getGp1().getLat(), congestRoad.getGp1().getLng()));
                                lstCongest.add(new GeoPoint(congestRoad.getGp2().getLat(), congestRoad.getGp2().getLng()));

                                lineCongest.getOutlinePaint().setColor(Color.parseColor("#CCFF0000"));
                                lineCongest.setPoints(lstCongest);
                                lineCongest.getOutlinePaint().setStrokeWidth(29F);
                                lineCongest.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
                                mapView.getOverlayManager().add(lineCongest);

                                mapController.animateTo(gp);
                                mapController.setZoom(18L);
                                alertGuide.setText("Chọn mức độ tắc nghẽn");
                                YoYo.with(Techniques.Bounce).duration(1000).playOn(alertGuide);
                                YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnLow);
                                YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnHigh);
                                YoYo.with(Techniques.SlideInUp).duration(500).playOn(btnMid);
                                alertStep2.setVisibility(View.VISIBLE);
                                alertStep2.setEnabled(true);
                            } else {
                                YoYo.with(Techniques.Bounce).duration(1500).playOn(alertGuide);
                            }
                        }
                    });

                }

                @Override
                public void onFailure(retrofit2.Call<Road> call, Throwable t) {

                }
            });
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

    private void sendAlert(int level) {
        String mobileId = getIMEIDeviceId();
        if (StringUtils.isEmpty(mobileId)) mobileId = "emulator0175";
        mAPIService.send(new AlertDTO(mobileId, congestRoad.getId(), level)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cảm ơn đóng góp của bạn!")
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
        track.setText("Bắt đầu");
        gps.disableFollowLocation();
        isTracking = false;
        mapView.getOverlayManager().remove(walkFrom);
        mapView.getOverlayManager().remove(line);
        mapView.getOverlayManager().remove(walkTo);
        YoYo.with(Techniques.SlideOutDown).duration(250).playOn(subCard);
        subCard.setEnabled(false);
    }

    private void showSubCardAndRoute() {
        mapView.getOverlayManager().add(walkFrom);
        mapView.getOverlayManager().add(line);
        mapView.getOverlayManager().add(walkTo);
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
        TAP_CODE = null;
        mapView.getOverlays().remove(lineCongest);
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