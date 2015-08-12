package np.com.smartsolutions.producttracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

    }

    @Override
    protected void onResume() {
        super.onResume();

        final Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent;
                            if (new UserHandler(SplashActivity.this).isLoggedIn())
                                intent = new Intent(SplashActivity.this, MainActivity.class);
                            else
                                intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        }
                    });
                }
            }
        };

        // Set animations
        final Animation iconEnter = AnimationUtils.loadAnimation(this, R.anim.rotate_icon);
        final Animation titleEnter = AnimationUtils.loadAnimation(this, R.anim.enter_splash_views);
        final Animation companyEnter = AnimationUtils.loadAnimation(this, R.anim.enter_splash_views);
        titleEnter.setStartOffset(500);
        companyEnter.setStartOffset(700);
        companyEnter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                timer.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        // Start animations
        findViewById(R.id.iv_icon).startAnimation(iconEnter);
        findViewById(R.id.tv_title).startAnimation(titleEnter);
        findViewById(R.id.tv_smartsolutions).startAnimation(companyEnter);

    }
}

