package com.runzii.cyou.common.constants;

/**
 * Created by runzii on 15-9-4.
 */
public enum SharedPreferencesUtilSettings {
    /**
     * 自动登录所需信息
     */
    SETTINGS_LOGIN_AUTO("com.runzii.cyou_account", ""),
    /**
     * 是否第一次使用
     */
    SETTINGS_FIRST_USE("com.runzii.cyou_first_use", Boolean.TRUE),
    /**
     * 新消息声音
     */
    SETTINGS_NEW_MSG_SOUND("com.runzii.cyou_new_msg_sound", true),
    /**
     * 新消息震动
     */
    SETTINGS_NEW_MSG_SHAKE("com.runzii.cyou_new_msg_shake", true),
    SETTINGS_APPKEY("com.runzii.cyou_appkey", "aaf98f894f73de3f014f97954bbe1788"),
    SETTINGS_TOKEN("com.runzii.cyou_token", "a76a1665807374d8839855c4d76ad988");


    private final String mId;
    private final Object mDefaultValue;

    SharedPreferencesUtilSettings(String mId, Object mDefaultValue) {
        this.mId = mId;
        this.mDefaultValue = mDefaultValue;
    }

    public String getId() {
        return mId;
    }

    public Object getDefaultValue() {
        return mDefaultValue;
    }

    public static SharedPreferencesUtilSettings fromId(String id) {
        SharedPreferencesUtilSettings[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }
}
