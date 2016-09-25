package com.runzii.cyou.common.utils;


import com.runzii.cyou.common.CYouAppManager;
import com.runzii.cyou.common.constants.SharedPreferencesUtilSettings;

/**
 * Created by Wouldyou on 2015/5/28.
 */
public class SharedPreferencesUtil {

    public static boolean getIsAutoLogin() {
        return CYouAppManager.getSharePreference().getBoolean("isAutoLogin", false);
    }

    public static boolean setIsAutoLogin(boolean b) {
        return CYouAppManager.getSharePreference().edit().putBoolean("isAutoLogin", b).commit();
    }


    public static boolean getIsRememberPassword() {
        return CYouAppManager.getSharePreference().getBoolean("isRememberPassword", false);
    }

    public static boolean setIsRememberPassword(boolean b) {
        return CYouAppManager.getSharePreference().edit().putBoolean("isRememberPassword", b).commit();
    }

    public static String getAutoLoginJson() {
        SharedPreferencesUtilSettings registAuto = SharedPreferencesUtilSettings.SETTINGS_LOGIN_AUTO;
        return CYouAppManager.getSharePreference().getString(registAuto.getId(), (String) registAuto.getDefaultValue());
    }

    public static boolean setAutoLoginJson(String json) {
        SharedPreferencesUtilSettings registAuto = SharedPreferencesUtilSettings.SETTINGS_LOGIN_AUTO;
        return CYouAppManager.getSharePreference().edit().putString(SharedPreferencesUtilSettings.SETTINGS_LOGIN_AUTO.getId(), json).commit();
    }

    public static boolean getIsNewMessageShake() {
        return CYouAppManager.getSharePreference().getBoolean(SharedPreferencesUtilSettings.SETTINGS_NEW_MSG_SHAKE.getId(), true);
    }

    public static boolean setIsNewMessageShake(boolean b) {
        return CYouAppManager.getSharePreference().edit().putBoolean(SharedPreferencesUtilSettings.SETTINGS_NEW_MSG_SHAKE.getId(), b).commit();
    }

    public static boolean getIsNewMessageSound() {
        return CYouAppManager.getSharePreference().getBoolean(SharedPreferencesUtilSettings.SETTINGS_NEW_MSG_SOUND.getId(), true);
    }

    public static boolean setIsNewMessageSound(boolean b) {
        return CYouAppManager.getSharePreference().edit().putBoolean(SharedPreferencesUtilSettings.SETTINGS_NEW_MSG_SOUND.getId(), b).commit();
    }


    public static boolean clearAll() {
        return CYouAppManager.getSharePreference().edit().clear().commit();
    }

}
