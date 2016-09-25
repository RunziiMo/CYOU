package com.runzii.cyou.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.easemob.EMCallBack;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.runzii.cyou.R;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.common.utils.LogUtil;
import com.runzii.cyou.common.utils.ToastUtil;
import com.runzii.cyou.ui.MainActivity;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * Created by runzii on 15-10-13.
 */
public class CYouAppManager {

    private static final String TAG = CYouAppManager.class.getSimpleName();

    public static Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();

    /**
     * Android 应用上下文
     */
    private static Context mContext = null;

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";

    //包名
    private static String pkgName = "com.runzii.cyou";

    public static String getPackageName() {
        return pkgName;
    }


    /**
     * 返回SharePreference配置文件名称
     *
     * @return
     */
    public static String getSharePreferenceName() {
        return pkgName + "_preferences";
    }

    public static SharedPreferences getSharePreference() {
        if (mContext != null) {
            return mContext.getSharedPreferences(getSharePreferenceName(), 0);
        }
        return null;
    }

    /**
     * 返回上下文对象
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }


    /**
     * 设置上下文对象
     *
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
        pkgName = context.getPackageName();
        LogUtil.d(pkgName);
    }

    /**
     * 获取应用程序版本名称
     *
     * @return
     */
    public static String getVersion() {
        String version = "0.0.0";
        if (mContext == null) {
            return version;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取应用版本号
     *
     * @return 版本号
     */
    public static int getVersionCode() {
        int code = 1;
        if (mContext == null) {
            return code;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return code;
    }

    /**
     * 打开浏览器下载新版本
     *
     * @param context
     */
    public static void startUpdater(Context context) {
        Uri uri = Uri.parse("http://dwz.cn/F8Amj");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void login(final String username, final LoginCallBack callBack) {

        EMChatManager.getInstance().login(username, "28465cnm", new EMCallBack() {

            @Override
            public void onSuccess() {
                // 登陆成功，保存用户名
                DemoHelper.getInstance().setCurrentUserName(username);
                // 注册群组和联系人监听
                DemoHelper.getInstance().registerGroupAndContactListener();

                // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                // ** manually load all local groups and
                EMGroupManager.getInstance().loadAllGroups();
                EMChatManager.getInstance().loadAllConversations();

                // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                        CYouAppManager.currentUserNick.trim());
                if (!updatenick) {
                    LogUtil.e("LoginActivity", "update current user nick fail");
                }
                //异步获取当前用户的昵称和头像(从自己服务器获取，demo使用的一个第三方服务)
                DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

                callBack.success();
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
                LogUtil.d(TAG, getContext().getString(R.string.Login_failed) + " " + code + " " + message);
                //打开注册页面
                if (code == -1005) {
                    try {
                        Looper.prepare();
                        // 调用sdk注册方法
                        EMChatManager.getInstance().createAccountOnServer(username, "28465cnm");
                        DemoHelper.getInstance().setCurrentUserName(username);
                        ToastUtil.showMessage(R.string.Registered_successfully);
                        login(username, callBack);
                    } catch (final EaseMobException e) {
                        int errorCode = e.getErrorCode();
                        if (errorCode == EMError.NONETWORK_ERROR) {
                            ToastUtil.showMessage(R.string.network_anomalies);
                        } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                            ToastUtil.showMessage(R.string.User_already_exists);
                        } else if (errorCode == EMError.UNAUTHORIZED) {
                            ToastUtil.showMessage(R.string.registration_failed_without_permission);
                        } else if (errorCode == EMError.ILLEGAL_USER_NAME) {
                            ToastUtil.showMessage(R.string.illegal_user_name);
                        } else {
                            ToastUtil.showMessage(getContext().getString(R.string.Registration_failed) + e.getMessage());
                        }
                    }
                } else {
                    callBack.failed(message);
                }

            }
        });


    }

    public static interface LoginCallBack {

        void success();

        void failed(String reason);

    }


}
