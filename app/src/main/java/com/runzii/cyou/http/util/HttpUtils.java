package com.runzii.cyou.http.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.easemob.chat.EMChatManager;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.common.utils.LogUtil;
import com.runzii.cyou.http.model.CheckUpdateData;
import com.runzii.cyou.http.model.CheckUpdateSuccessData;
import com.runzii.cyou.http.model.GetBaiduPictureData;
import com.runzii.cyou.http.model.GetBaiduPictureSuccessData;
import com.runzii.cyou.http.model.LoginData;
import com.runzii.cyou.http.model.LoginSuccessData;
import com.runzii.cyou.http.model.PushInfoData;
import com.runzii.cyou.http.model.PushInfoSuccessData;
import com.runzii.cyou.http.model.RandomVideoChatSuccessData;
import com.runzii.cyou.http.model.RandomVideoData;
import com.runzii.cyou.http.model.RegistData;
import com.runzii.cyou.http.model.RegistSuccessData;
import com.runzii.cyou.http.model.SuggestData;
import com.runzii.cyou.http.model.SuggestSuccessData;
import com.runzii.cyou.common.CYouAppManager;
import com.runzii.cyou.common.utils.SharedPreferencesUtil;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();

    private static AsyncHttpClient httpClient;
    private static SyncHttpClient syncHttpClient;

    public static void init(Context context) {
        httpClient = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        httpClient.setCookieStore(cookieStore);
        httpClient.setEnableRedirects(true, true, true);
        syncHttpClient = new SyncHttpClient();
        syncHttpClient.setEnableRedirects(true, true, true);
    }

    public static void CancelHttpTask() {
        httpClient.cancelAllRequests(true);
    }


    public static void CheckUpdate(CheckUpdateData data, Context context,
                                   final HttpCallBack callBack) {
        httpClient.post(HttpConstants.CHECKUPDATEURL,
                new TextHttpResponseHandler() {

                    @Override
                    public void onSuccess(int arg0, Header[] arg1, String arg2) {
                        CheckUpdateSuccessData updateSuccessData = new Gson().fromJson(arg2, CheckUpdateSuccessData.class);
                        callBack.onSuccess(updateSuccessData);
                    }

                    @Override
                    public void onFailure(int arg0, Header[] arg1, String arg2,
                                          Throwable arg3) {
                        callBack.onError(arg0);
                    }
                });
    }

    public static void Login(final LoginData data, Context context,
                             final HttpCallBack callBack) {
//        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
//        List<Cookie> cookies = myCookieStore.getCookies();
//        for (Cookie cookie : cookies) {
//            ToastUtil.show(cookie.getName() + " = " + cookie.getValue());
//        }
        httpClient.post(context, HttpConstants.LOGINURL, data.toStringEntity(),
                HttpConstants.contentType, new JsonHttpResponseHandler() {


                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        callBack.onError(statusCode);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        SharedPreferences.Editor editor = CYouAppManager.getSharePreference().edit();
                        if ("401".equals(response.toString())) {
                            callBack.onFailed();
                            SharedPreferencesUtil.clearAll();
                        } else {
                            LoginSuccessData loginSuccessData = new Gson().fromJson(response.toString(), LoginSuccessData.class);
                            callBack.onSuccess(loginSuccessData);
                        }
                    }

                });
    }


    public static void Suggest(SuggestData data, Context context,
                               final HttpCallBack callBack) throws UnsupportedEncodingException {
        data.setUserid(DemoHelper.getInstance().getCurrentUsernName());
        httpClient
                .post(context, HttpConstants.SUGGESTUSURL,
                        new StringEntity(new Gson().toJson(data)),
                        HttpConstants.contentType,
                        new TextHttpResponseHandler() {

                            @Override
                            public void onSuccess(int arg0, Header[] arg1, String arg2) {
                                callBack.onSuccess(new SuggestSuccessData());
                            }

                            @Override
                            public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
                                callBack.onError(arg0);
                            }
                        });
    }


    public static void Regist(RegistData data, Context context,
                              final HttpCallBack callBack) throws UnsupportedEncodingException {

        httpClient.post(context, HttpConstants.REGISTERURL, new StringEntity(data.getRegistdata()), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callBack.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callBack.onSuccess(new RegistSuccessData(responseString));
            }

        });

    }


    public static void SetPushInfo(PushInfoData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        data.setUid(DemoHelper.getInstance().getCurrentUsernName());
        httpClient.post(context, HttpConstants.SETPUSHINFO, new StringEntity(new Gson().toJson(data)), HttpConstants.contentType, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.onSuccess(new PushInfoSuccessData(responseString));
            }
        });
    }

    public static void getBaiduPicture(GetBaiduPictureData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        httpClient.post(HttpConstants.GETBAIDUPICTURE, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.onSuccess(new Gson().fromJson(responseString, GetBaiduPictureSuccessData.class));
            }
        });
    }

    public static void randomVideoChat(RandomVideoData data, Context context, final HttpCallBack callback) throws UnsupportedEncodingException {
        httpClient.get(HttpConstants.RANDOMCALL + EMChatManager.getInstance().getCurrentUser() + "/videoRandomCall/", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onError(statusCode);
                LogUtil.d(TAG, responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (statusCode == 200) {
                    callback.onSuccess(new Gson().fromJson(responseString, RandomVideoChatSuccessData.class));
                } else {
                    callback.onError(statusCode);
                }
            }
        });
    }

    public static void MakeAPICall(Object data, Context context, final HttpCallBack callback) {
        try {
            if (data instanceof CheckUpdateData) {
                CheckUpdate((CheckUpdateData) data, context, callback);
            } else if (data instanceof LoginData) {
                Login((LoginData) data, context, callback);
            } else if (data instanceof PushInfoData) {
                SetPushInfo((PushInfoData) data, context, callback);
            } else if (data instanceof RegistData) {
                Regist((RegistData) data, context, callback);
            } else if (data instanceof SuggestData) {
                Suggest((SuggestData) data, context, callback);
            } else if (data instanceof GetBaiduPictureData) {
                getBaiduPicture((GetBaiduPictureData) data, context, callback);
            }else if (data instanceof RandomVideoData){
                randomVideoChat((RandomVideoData) data,context,callback);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}