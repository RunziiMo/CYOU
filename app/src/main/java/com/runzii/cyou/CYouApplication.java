package com.runzii.cyou;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.runzii.cyou.common.CYouAppManager;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.http.util.HttpUtils;

import java.io.File;

import cn.smssdk.SMSSDK;

public class CYouApplication extends Application {

    private static final String TAG = CYouApplication.class.getSimpleName();


    private static CYouApplication instance;


    private static Context ct;

    public static Context getCYouApplicationContext() {
        return ct;
    }

    public static CYouApplication getInstance() {
        if (instance == null) {
            Log.w(TAG, "[CYouApplication] instance is null.");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        CYouAppManager.setContext(instance);

        initImageLoader();

        ct = getApplicationContext();

        HttpUtils.init(this);

        DemoHelper.getInstance().init(ct);

        SMSSDK.initSDK(ct, "b2531641e8f8", "10b998780d3ce6268fde0f32ccbee152");

    }


    private void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "ECSDK_Demo/image");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .threadPoolSize(1)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new WeakMemoryCache())
                        // .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(CYouAppManager.md5FileNameGenerator)
                        // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiscCache(cacheDir, null, CYouAppManager.md5FileNameGenerator))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        // .writeDebugLogs() // Remove for release app
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }


}
