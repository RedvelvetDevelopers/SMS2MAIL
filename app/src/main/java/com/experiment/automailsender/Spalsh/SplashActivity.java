package com.experiment.automailsender.Spalsh;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.experiment.automailsender.MainActivity;
import com.experiment.automailsender.R;


/**
 * Created by Sananda on 10-07-2018.
 */

public class SplashActivity extends Activity {
;
    private static final long TIME_OUT_MILI = 3000;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


   StartAnimations();

    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);

        iv.clearAnimation();
        iv.startAnimation(anim);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                   startActivity(new Intent(SplashActivity.this, MainActivity.class));

                finish();
            }
        }, TIME_OUT_MILI);
    }

}