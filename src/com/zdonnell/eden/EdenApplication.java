package com.zdonnell.eden;

import java.io.File;

import android.app.Application;
import android.os.AsyncTask;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.zdonnell.eden.staticdata.CheckServerDataTask;

public class EdenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        configureUILSingleton();
        checkStaticData();
    }

    private void configureUILSingleton() {
        File cacheDir = StorageUtils.getCacheDirectory(getBaseContext());

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();

        // Create global configuration and initialize ImageLoader with this
        // configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024).discCache(new UnlimitedDiscCache(cacheDir)) // default
                .defaultDisplayImageOptions(defaultOptions) // default
                .build();

        ImageLoader.getInstance().init(config);
    }

    private void checkStaticData() {
        new CheckServerDataTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
