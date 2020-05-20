package com.core.framework.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Process;

import com.core.framework.develop.DevRunningTime;
import com.core.framework.develop.LogUtil;
import com.core.framework.net.ApiDns;
import com.core.framework.net.NetworkWorker;
import com.core.framework.net.SSLSocketClient;
import com.core.framework.netLib.HeaderInterceptor;
import com.core.framework.store.sharePer.PreferencesUtils;
import com.core.framework.util.StringUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.OkHttpClient;

public abstract class MyApplication extends Application {
    public static MyApplication instance;
    public static int netType;
    public static boolean netChanged;
    private List<Activity> mActivityList = new ArrayList<Activity>();
    int myPid = -1;

    public static boolean isLogin = false;


    @Override
    public void onCreate() {
        super.onCreate();
        //防止被services启动，多次初始化
        if (!isApplicationProcess(this)) {
            return;
        }
        instance = this;
        initHttp();
        myPid = android.os.Process.myPid();
        mActivityList.clear();
        doBusyTransaction();
        if (DevRunningTime.isTaoBaoProessNotDBinit && isWebProcess()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doBackTransaction();
                checkService();
            }
        }).start();
    }


    public static MyApplication getInstance() {
        return instance;
    }

    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }


    public boolean isNewVison() {
        int thisVison = getVersionCode();
        int dbVison = PreferencesUtils.getInteger("current_app_vison");
        return thisVison != dbVison;
    }

    public int getVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getTruePackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getTruePackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public abstract void checkService();

    public abstract void doBusyTransaction();

    public abstract void doBackTransaction();

    public static final String WEB = "tbwebpro";
    public static String WEB_PACKAGE = "com.android.browser";

    public String getTruePackageName() {
//        return "com.boju.hiyo";
        return super.getPackageName();
    }

    @Override
    public String getPackageName() {
        if (isWebProcess() && Integer.valueOf(android.os.Build.VERSION.SDK).intValue() < 21) {
            return WEB_PACKAGE;
        }
        return super.getPackageName();
    }

    public boolean isMainAppPro() {
        String info = getCurProcessName();
        return (!StringUtil.isEmpty(info) && info.equals(super.getPackageName()));
    }

    public boolean isWebProcess() {
        String info = getCurProcessName();
        return (!StringUtil.isEmpty(info) && info.contains(WEB));
    }

    boolean isLoadRunningAppProcessInfo;

    public String getCurProcessName() {
        try {
            int pid = Process.myPid();
            Object oo = getSystemService(Context.ACTIVITY_SERVICE);
            if (oo == null) return null;
            ActivityManager mActivityManager = (ActivityManager) oo;
            if (oo == null) return null;
            if (isLoadRunningAppProcessInfo) return null;
            isLoadRunningAppProcessInfo = true;
            List<ActivityManager.RunningAppProcessInfo> list = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                list = mActivityManager.getRunningAppProcesses();
            }
            isLoadRunningAppProcessInfo = false;
            if (list == null) return null;
            for (ActivityManager.RunningAppProcessInfo appProcess : list) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    if (appProcess.pid == pid) {
                        return appProcess.processName;
                    }
                }
            }
        } catch (Exception e) {
//            LogUtil.w(e);
        }
        return null;
    }

    public void exit() {
        for (Activity activity : mActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        mActivityList.clear();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.debug("app", "app启动  onConfigurationChanged" + newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtil.debug("app", "app启动 onLowMemory ");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.debug("app", "app启动 onTerminate ");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtil.debug("app", "app启动 onTrimMemory level " + level);
    }


    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     *
     * @return
     */
    private boolean isApplicationProcess(Context context) {
        int pID = Process.myPid();
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK等被初始化2次，导致的某些问题，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回false
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List l = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            l = am.getRunningAppProcesses();
        }
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                info = (ActivityManager.RunningAppProcessInfo) (i.next());
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    if (info.pid == pID) {
                        CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                        processName = info.processName;
                        if (processName == null || !processName.equalsIgnoreCase(getPackageName())) {
                            // 则此application::onCreate 是被service 调用的，直接返回
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (processName == null || !processName.equalsIgnoreCase(getPackageName())) {
            // 则此application::onCreate 是被service 调用的，直接返回
            return false;
        }
        return true;
    }

    private void initHttp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dns(new ApiDns())
                .connectTimeout(51, TimeUnit.SECONDS)
                .readTimeout(202, TimeUnit.SECONDS)
                .addInterceptor(new HeaderInterceptor())
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .hostnameVerifier(SSLSocketClient.getHostNameVerifier())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    public abstract void net401(String requestUrl, String method, Object hasMapParam, Object[] param, NetworkWorker.ICallback callback);
}
