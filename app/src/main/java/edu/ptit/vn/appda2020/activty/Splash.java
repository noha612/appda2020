package edu.ptit.vn.appda2020.activty;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import edu.ptit.vn.appda2020.R;
import edu.ptit.vn.appda2020.util.CommonUtils;

public class Splash extends AppCompatActivity {
    TextView splashAppName;

    Thread startUp = new Thread() {
        @Override
        public void run() {
            try {
                super.run();
                sleep(2500);
            } catch (Exception e) {
                Log.v("error", e.toString());
            } finally {
                Intent i = new Intent(Splash.this, MainActivity.class);
                startActivityForResult(i, 99);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashAppName = findViewById(R.id.splashAppName);
        splashAppName.setVisibility(View.INVISIBLE);

        CommonUtils.setTranslucentStatus(this, true);
        CommonUtils.MIUISetStatusBarLightMode(this, true);
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
            splashAppName.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInDown).duration(2500).playOn(splashAppName);
            startUp.start();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == 0) {
                splashAppName.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).duration(2000).playOn(splashAppName);
                startUp.start();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}