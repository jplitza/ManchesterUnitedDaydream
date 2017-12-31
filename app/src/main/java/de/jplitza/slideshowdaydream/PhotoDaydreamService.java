package de.jplitza.slideshowdaydream;

import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PhotoDaydreamService extends DreamService {

    ViewAnimator ivs;
    List<String> urls = new ArrayList<>();
    Picasso picasso;
    OkHttpClient client = new OkHttpClient();
    int i = 0;
    android.os.Handler imageSwitchHandler;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            ImageView iv = (ImageView) ivs.getChildAt((ivs.getDisplayedChild() + 1) % ivs.getChildCount());
            Picasso.with(PhotoDaydreamService.this)
                    .load(urls.get(i))
                    .noFade()
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            ivs.showNext();
                        }

                        @Override
                        public void onError() {
                            imageSwitchHandler.post(r);
                        }
                    });

            i++;
            if (i >= urls.size()) {
                i = 0;
            }
            imageSwitchHandler.removeCallbacks(r);
            imageSwitchHandler.postDelayed(r, PhotoDaydreamService.this.getResources().getInteger(R.integer.displayDuration));
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        super.onDreamingStarted();
        setContentView(R.layout.dream_view);
        ivs = (ViewAnimator) findViewById(R.id.ivPictures);

        // set animations
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(this.getResources().getInteger(R.integer.fadeDuration));
        ivs.setOutAnimation(fadeOut);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(this.getResources().getInteger(R.integer.fadeDuration));
        ivs.setInAnimation(fadeIn);

        new Thread(getData).start();
    }

    Runnable getData = new Runnable() {
        @Override
        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PhotoDaydreamService.this);
            String url = prefs.getString("url", null);
            if (url == null) {
                Log.e("PhotoDaydreamService", "Empty url");
                return;
            }
            Log.d("PhotoDaydreamService", String.format("Loading URL %s", url));
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                ResponseBody response = client.newCall(request).execute().body();
                if (response == null || response.toString().isEmpty()) {
                    Log.e("PhotoDaydreamService", "Empty response");
                    return;
                }
                urls.addAll(Arrays.asList(response.string().split("\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageSwitchHandler = new android.os.Handler(Looper.getMainLooper());
            imageSwitchHandler.postDelayed(r, 0);
        }
    };

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        imageSwitchHandler.removeCallbacks(r);
        imageSwitchHandler = null;
    }
}
