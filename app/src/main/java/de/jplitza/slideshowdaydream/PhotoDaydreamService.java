package de.jplitza.slideshowdaydream;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class PhotoDaydreamService extends DreamService {

    ImageView iv;
    List<String> urls = new ArrayList<>();
    Picasso picasso;
    OkHttpClient client = new OkHttpClient();
    int i = 0;
    android.os.Handler imageSwitchHandler;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            picasso.with(PhotoDaydreamService.this).load(urls.get(i)).into(iv);
            i++;
            if (i >= urls.size()) {
                i = 0;
            }
            imageSwitchHandler.removeCallbacks(r);
            imageSwitchHandler.postDelayed(r, 10000);
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        super.onDreamingStarted();
        setContentView(R.layout.dream_view);
        iv = (ImageView) findViewById(R.id.ivPicture);

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

            picasso = new Picasso.Builder(PhotoDaydreamService.this).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();
                }
            }).build();

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
