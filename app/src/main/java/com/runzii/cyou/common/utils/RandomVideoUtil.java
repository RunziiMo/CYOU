package com.runzii.cyou.common.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.runzii.cyou.R;
import com.runzii.cyou.common.CYouAppManager;
import com.runzii.cyou.http.model.RandomVideoChatSuccessData;
import com.runzii.cyou.http.model.RandomVideoData;
import com.runzii.cyou.http.util.HttpCallBack;
import com.runzii.cyou.http.util.HttpUtils;
import com.runzii.cyou.ui.call.VideoCallActivity;

/**
 * Created by runzii on 15-10-14.
 */
public class RandomVideoUtil {


    private static boolean autoRerandom = false;


    public static void stopRandom() {
        autoRerandom = false;
    }

    public static void randomVideo(final Context context) {

        autoRerandom = true;

        final MaterialDialog randomDialog = new MaterialDialog.Builder(context)
                .content(R.string.http_randomvideo).cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        stopRandom();
                    }
                })
                .progress(true, -1).build();


        makeCall(context, randomDialog);

        randomDialog.show();
    }

    private static void makeCall(final Context context, final MaterialDialog randomDialog) {
        HttpUtils.MakeAPICall(new RandomVideoData(), CYouAppManager.getContext(), new HttpCallBack() {
            @Override
            public void onSuccess(Object object) {
                RandomVideoChatSuccessData successData = (RandomVideoChatSuccessData) object;
                dialTo(context, successData.getTo());
                randomDialog.dismiss();
            }

            @Override
            public void onFailed() {

            }

            @Override
            public void onError(int statusCode) {
                if (statusCode == 200) {
                    ToastUtil.showMessage("匹配到了，忘了谁了，对不起～");
                } else if (statusCode == 400) {
                    if (autoRerandom) {
                        makeCall(context, randomDialog);
                        LogUtil.d(LogUtil.getLogUtilsTag(RandomVideoUtil.class), "没搞到，再来一发");
                    } else {
                        randomDialog.dismiss();
                    }
                }
            }
        });

    }

    public static void dialTo(Context context, String to) {
        if (context == null) {
            ToastUtil.showMessage("yo");
        } else if (to.equals(DemoHelper.getInstance().getCurrentUsernName())) {
            ToastUtil.showMessage("你不能和自己通话");
        } else {
            context.startActivity(new Intent(context, VideoCallActivity.class).putExtra("username", to)
                    .putExtra("isComingCall", false));
        }

    }

    public static boolean isAutoRerandom() {
        return autoRerandom;
    }
}
