package com.runzii.cyou.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.runzii.cyou.R;
import com.runzii.cyou.common.CYouAppManager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CommonDialog;
import cn.smssdk.gui.layout.Res;
import cn.smssdk.gui.layout.SendMsgDialogLayout;

import static com.mob.tools.utils.R.getStringRes;
import static com.mob.tools.utils.R.getStyleRes;

/**
 * Created by runzii on 15-10-15.
 */
public class SMSSDKUtil {

    private OnSendMessageHandler osmHandler;
    private EventHandler callback;

    private static SMSSDKUtil instance;

    public static SMSSDKUtil getInstance() {
        if (instance == null) {
            instance = new SMSSDKUtil();
            instance.callback = new EventHandler() {
                public void afterEvent(int event, int result, Object data) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                        String country = (String) phoneMap.get("country");
                        final String phone = (String) phoneMap.get("phone");

                    }
                }
            };
        }
        return instance;
    }

    private SMSSDKUtil() {
    }

    public void setOnSendMessageHandler(OnSendMessageHandler h) {
        osmHandler = h;
    }

    // 默认使用中国区号
    private static final String DEFAULT_COUNTRY_ID = "42";

    public String[] getCurrentCountry() {
        String mcc = getMCC();
        String[] country = null;
        if (!TextUtils.isEmpty(mcc)) {
            country = SMSSDK.getCountryByMCC(mcc);
        }

        if (country == null) {
            LogUtil.w("SMSSDK", "no country found by MCC: " + mcc);
            country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
        }
        return country;
    }

    public String getMCC() {
        TelephonyManager tm = (TelephonyManager) CYouAppManager.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        // 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
        String networkOperator = tm.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            return networkOperator;
        }

        // 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
        return tm.getSimOperator();
    }

    /**
     * 是否请求发送验证码，对话框
     */
    public void sendVerificationCode(final String phone, final String code) {
        LogUtil.e("verification phone ==>>", phone);
        SMSSDK.getVerificationCode(code, phone.trim(), osmHandler);
    }


    /**
     * 分割电话号码
     */
    public String splitPhoneNum(String phone) {
        StringBuilder builder = new StringBuilder(phone);
        builder.reverse();
        for (int i = 4, len = builder.length(); i < len; i += 5) {
            builder.insert(i, ' ');
        }
        builder.reverse();
        return builder.toString();
    }

}
