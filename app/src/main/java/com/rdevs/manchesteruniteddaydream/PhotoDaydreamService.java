package com.rdevs.manchesteruniteddaydream;

import android.net.Uri;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.widget.ImageView;

import com.squareup.picasso.Picasso   ;

import java.util.ArrayList;
import java.util.List;

public class PhotoDaydreamService extends DreamService {

    ImageView iv;
    List<String> urls = new ArrayList<>();
    Picasso picasso;
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
            imageSwitchHandler.postDelayed(r, 3000);
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

        fillData();

        picasso = new Picasso.Builder(this).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        }).build();

        imageSwitchHandler = new android.os.Handler(Looper.getMainLooper());
        imageSwitchHandler.postDelayed(r, 0);
    }

    private void fillData() {
        urls.add("https://s-media-cache-ak0.pinimg.com/564x/c7/2a/17/c72a179ff2ae889bbeeeca117eac47dd.jpg");
        urls.add("http://cdn.images.dailystar.co.uk/dynamic/122/photos/270000/900x738/909270.jpg");
        urls.add("https://s-media-cache-ak0.pinimg.com/736x/aa/00/fc/aa00fc5afc750a13693d690eb3e9f3f5.jpg");
        urls.add("https://s-media-cache-ak0.pinimg.com/736x/08/de/d2/08ded2b8cfc33528287a59e287c25b06.jpg");
        urls.add("https://s-media-cache-ak0.pinimg.com/originals/8b/95/be/8b95be417306e749cae29c61a725de39.jpg");
        urls.add("https://s-media-cache-ak0.pinimg.com/236x/57/44/a9/5744a96bc8d6f549a26c12587ab7caf1.jpg");
        urls.add("https://s-media-cache-ak0.pinimg.com/originals/f3/33/fc/f333fce4af92a46c1c54649e205a34c7.jpg");
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        imageSwitchHandler.removeCallbacks(r);
        imageSwitchHandler = null;
    }
}
