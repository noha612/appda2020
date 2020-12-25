package edu.ptit.vn.appda2020.activty;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import edu.ptit.vn.appda2020.R;
import edu.ptit.vn.appda2020.util.CommonUtils;

public class SendAlertActivity extends AppCompatActivity {
    IMapController mapController;
    MapView mapView;
    MyLocationNewOverlay gps;
    Button alertBackToMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_alert);
        initMap();
        alertBackToMain = findViewById(R.id.alertBackToMain);
        alertBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initMap() {

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = findViewById(R.id.mapSendAlert);
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
    }
}