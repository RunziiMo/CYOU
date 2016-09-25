package com.runzii.cyou.ui.init;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.runzii.cyou.R;
import com.runzii.cyou.common.CYouAppManager;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.common.utils.SMSSDKUtil;
import com.runzii.cyou.common.utils.ToastUtil;
import com.runzii.cyou.common.view.KeyBoardUtil;
import com.runzii.cyou.ui.MainActivity;
import com.runzii.cyou.ui.base.BaseActivity;
import com.vstechlab.easyfonts.EasyFonts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import cn.smssdk.utils.SMSLog;

import static com.mob.tools.utils.R.getStringRes;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements TextWatcher {


    private static final int RETRY_INTERVAL = 60;

    // UI references.
    private EditText mNumberView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private KeyBoardUtil mKeyBoard;
    private KenBurnsView mKenBurnView;
    private Button mWeiboButton, mLoginButton;
    private TextView appInfo;
    private MaterialDialog mProgressDialog;

    private EventHandler handler, identifyhandler;

    private TextView tvCountryNum;

    private String currentCode;

    private int time = RETRY_INTERVAL;

    // 国家号码规则
    private HashMap<String, String> countryRules;

    private boolean autoLogin = false;

    private BroadcastReceiver smsReceiver;

    @Override
    protected boolean isHideNavigationBar() {
        return false;
    }

    @Override
    protected boolean isDisplayHomeAsUp() {
        return false;
    }

    @Override
    protected boolean isEnableSwipe() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 如果登录成功过，直接进入主页面
        if (DemoHelper.getInstance().isLoggedIn()) {
            autoLogin = true;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));

            return;
        }

        mProgressDialog = new MaterialDialog.Builder(this)
                .content("正在加载").cancelable(false)
                .progress(true, 0).build();

        String[] country = SMSSDKUtil.getInstance().getCurrentCountry();
        // String[] country = SMSSDK.getCountry(currentId);
        if (country != null) {
            currentCode = country[1];
            currentCode = "86";
        }

        tvCountryNum = (TextView) findViewById(R.id.tv_country_num);
        tvCountryNum.setText("+" + currentCode);
        // Set up the login form.
        mNumberView = (EditText) findViewById(R.id.number);
        mNumberView.addTextChangedListener(this);


        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.email_login_form);
        mKenBurnView = (KenBurnsView) findViewById(R.id.login_background);
        mWeiboButton = (Button) findViewById(R.id.regist_button);
        mLoginButton = (Button) findViewById(R.id.login_button);
        appInfo = (TextView) findViewById(R.id.app_info_brief);

        appInfo.setTypeface(EasyFonts.caviarDreams(this));

        mKeyBoard = new KeyBoardUtil(this, getApplicationContext(), mNumberView);

        if (DemoHelper.getInstance().getCurrentUsernName() != null) {
            mNumberView.setText(DemoHelper.getInstance().getCurrentUsernName());
        }


        mNumberView.setShowSoftInputOnFocus(false);
        mPasswordView.setShowSoftInputOnFocus(false);

        mLoginFormView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                performKeyBoardShowOrHide(false);
                return false;
            }
        });

        mNumberView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mKeyBoard.setEd(mNumberView);
                performKeyBoardShowOrHide(true);
                return false;
            }
        });

        mPasswordView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mKeyBoard.setEd(mPasswordView);
                return false;
            }
        });

        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    attemptLogin();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mWeiboButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showMessage("暂未开通");
            }
        });

        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performKeyBoardShowOrHide(true);
            }
        });

        handler = new EventHandler() {
            @SuppressWarnings("unchecked")
            public void afterEvent(final int event, final int result,
                                   final Object data) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                                // 请求支持国家列表
                                onCountryListGot((ArrayList<HashMap<String, Object>>) data);
                            } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                // 请求验证码后，跳转到验证码填写页面
                                boolean smart = (Boolean) data;
                            }
                        } else {
                            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE
                                    && data != null
                                    && (data instanceof UserInterruptException)) {
                                // 由于此处是开发者自己决定要中断发送的，因此什么都不用做
                                return;
                            }

                            // 根据服务器返回的网络错误，给toast提示
                            try {
                                ((Throwable) data).printStackTrace();
                                Throwable throwable = (Throwable) data;

                                JSONObject object = new JSONObject(
                                        throwable.getMessage());
                                String des = object.optString("detail");
                                if (!TextUtils.isEmpty(des)) {
                                    ToastUtil.showMessage(des);
                                    return;
                                }
                            } catch (Exception e) {
                                SMSLog.getInstance().w(e);
                            }
                            // 如果木有找到资源，默认提示
                            int resId = getStringRes(LoginActivity.this,
                                    "smssdk_network_error");
                            if (resId > 0) {
                                ToastUtil.showMessage(resId);
                            }
                        }
                    }
                });
            }
        };

        identifyhandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    /** 提交验证码 */
                    afterSubmit(result, data);
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    /** 获取验证码成功后的执行动作 */
                    afterGet(result, data);
                } else if (event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
                    /** 获取语音版验证码成功后的执行动作 */
//                    afterGetVoice(result, data);
                }
            }
        };
        SMSSDK.registerEventHandler(identifyhandler);

//        try {
//            if (DeviceHelper.getInstance(this).checkPermission("android.permission.RECEIVE_SMS")) {
//                smsReceiver = new SMSReceiver(new SMSSDK.VerifyCodeReadListener() {
//                    public void onReadVerifyCode(final String verifyCode) {
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                mPasswordView.setText(verifyCode);
//                            }
//                        });
//                    }
//                });
//                registerReceiver(smsReceiver, new IntentFilter(
//                        "android.provider.Telephony.SMS_RECEIVED"));
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//            smsReceiver = null;
//        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    private void performKeyBoardShowOrHide(boolean isShow) {
        if (isShow) {
            if (mWeiboButton.getVisibility() == View.VISIBLE) {
                mWeiboButton.setVisibility(View.GONE);
            } else {
                return;
            }
            findViewById(R.id.keyboard_frame).setVisibility(View.VISIBLE);
            mKeyBoard.showKeyboard();
            mNumberView.setHint(R.string.prompt_phone);
            mLoginButton.setText(R.string.action_send_message);
            if (mNumberView.getText().length() == 11) {
                mLoginButton.setEnabled(true);
                mLoginButton.setBackgroundResource(R.drawable.selector_alpha_button_orange);
            } else {
                mLoginButton.setEnabled(false);
                mLoginButton.setBackgroundResource(R.color.black_alpha_1);
            }
            mLoginButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SMSSDKUtil.getInstance().sendVerificationCode(mNumberView.getText().toString(), currentCode);
                }
            });
        } else {
            if (mWeiboButton.getVisibility() == View.GONE) {
                mWeiboButton.setVisibility(View.VISIBLE);
            } else {
                return;
            }
            findViewById(R.id.keyboard_frame).setVisibility(View.GONE);
            mNumberView.setHint(R.string.prompt_use_phone);
            mLoginButton.setText(R.string.action_sign_in_short);
            mLoginButton.setEnabled(true);
            mLoginButton.setBackgroundResource(R.drawable.selector_alpha_button_orange);
            mLoginButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    performKeyBoardShowOrHide(true);
                }
            });
        }
    }

    /**
     * 倒数计时
     */
    private void countDown() {
        runOnUiThread(new Runnable() {

            public void run() {
                time--;
                if (time == 0) {
                    if (findViewById(R.id.keyboard_frame).getVisibility() == View.VISIBLE) {
                        mLoginButton.setText(R.string.action_send_message);
                        mLoginButton.setEnabled(true);
                        mLoginButton.setBackgroundResource(R.drawable.selector_alpha_button_orange);
                    }
                    time = RETRY_INTERVAL;
                } else {
                    if (findViewById(R.id.keyboard_frame).getVisibility() == View.VISIBLE) {
                        mLoginButton.setText(time + "");
//					if (time == 30){
//						btnSounds.setVisibility(View.VISIBLE);
//					}
                        mLoginButton.setBackgroundResource(R.color.black_alpha_1);
                        mLoginButton.setEnabled(false);
                    }
                    new Handler().postDelayed(this, 1000);
                }
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        if (!EaseCommonUtils.isNetWorkConnected(this)) {
            ToastUtil.showMessage(R.string.network_isnot_available);
            return;
        }

        hideSoftKeyboard();

        // Reset errors.
        mNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String phone = mNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mNumberView.setError(getString(R.string.error_invalid_email));
            focusView = mNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressDialog.show();
            String code = tvCountryNum.getText().toString().trim();
            if (code.startsWith("+")) {
                code = code.substring(1);
            }
            SMSSDK.submitVerificationCode(code, phone, password);
        }
    }

    private boolean isPhoneValid(String phone) {
        Pattern p = Pattern.compile(getResources().getString(R.string.regExpPhoneNumber));
        Matcher m = p.matcher(phone);
        return m.find();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() == 4;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SMSSDK.registerEventHandler(handler);
        if (autoLogin) {
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SMSSDK.unregisterEventHandler(handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(identifyhandler);
        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mNumberView.setError(null);
        if (isPhoneValid(s.toString())) {
            mLoginButton.setEnabled(true);
            mLoginButton.setBackgroundResource(R.drawable.selector_alpha_button_orange);
        } else {
            mLoginButton.setEnabled(false);
            mLoginButton.setBackgroundResource(R.color.black_alpha_1);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
        // 解析国家列表
        for (HashMap<String, Object> country : countries) {
            String code = (String) country.get("zone");
            String rule = (String) country.get("rule");
            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                continue;
            }

            if (countryRules == null) {
                countryRules = new HashMap<String, String>();
            }
            countryRules.put(code, rule);
        }
        // 检查手机号码
        String phone = mNumberView.getText().toString().trim().replaceAll("\\s*", "");
        String code = tvCountryNum.getText().toString().trim();
        checkPhoneNum(phone, code);
    }

    /**
     * 检查电话号码
     */
    private void checkPhoneNum(String phone, String code) {
        if (code.startsWith("+")) {
            code = code.substring(1);
        }

        if (TextUtils.isEmpty(phone)) {
            ToastUtil.showMessage("手机号不能为空");
            return;
        }

        if (countryRules == null || countryRules.size() <= 0) {
            if (code == "86") {
                if (isPhoneValid(phone)) {
                    ToastUtil.showMessage("手机号格式不正确");
                    return;
                }
                SMSSDKUtil.getInstance().sendVerificationCode(phone, code);
            } else {
                ToastUtil.showMessage("不支持的国家和地区");
            }
            return;
        }

        String rule = countryRules.get(code);
        Pattern p = Pattern.compile(rule);
        Matcher m = p.matcher(phone);
        int resId = 0;
        if (!m.matches()) {
            ToastUtil.showMessage("手机号格式个国家不匹配");
            return;
        }
        SMSSDKUtil.getInstance().sendVerificationCode(phone, code);
    }


    /**
     * 提交验证码成功后的执行事件
     *
     * @param result
     * @param data
     */
    private void afterSubmit(final int result, final Object data) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                if (result == SMSSDK.RESULT_COMPLETE) {
                    mProgressDialog.setContent("正在登录");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    String phone = (String) phoneMap.get("phone");
                    CYouAppManager.login(phone, new CYouAppManager.LoginCallBack() {
                        @Override
                        public void success() {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void failed(String reason) {
                            ToastUtil.showMessage(reason);
                        }
                    });

                } else {
                    ((Throwable) data).printStackTrace();
                    // 验证码不正确
                    String message = ((Throwable) data).getMessage();
                    int resId = 0;
                    try {
                        JSONObject json = new JSONObject(message);
                        int status = json.getInt("status");
                        resId = getStringRes(LoginActivity.this,
                                "smssdk_error_detail_" + status);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (resId == 0) {
                        resId = getStringRes(LoginActivity.this, "smssdk_virificaition_code_wrong");
                    }
                    if (resId > 0) {
                        ToastUtil.showMessage(resId);
                    }
                }
            }
        });
    }

    /**
     * 获取验证码成功后,的执行动作
     *
     * @param result
     * @param data
     */
    private void afterGet(final int result, final Object data) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                if (result == SMSSDK.RESULT_COMPLETE) {
                    int resId = getStringRes(LoginActivity.this,
                            "smssdk_virificaition_code_sent");
                    if (resId > 0) {
                        ToastUtil.showMessage(resId);
                    }
                    resId = getStringRes(LoginActivity.this, "smssdk_receive_msg");
                    if (resId > 0) {
                        String unReceive = getString(resId, time);
//                        ToastUtil.showMessage(unReceive);
                        ToastUtil.showMessage("验证码已发送");
                    }
                    time = RETRY_INTERVAL;
                    countDown();
                    mPasswordView.setVisibility(View.VISIBLE);
                } else {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    // 根据服务器返回的网络错误，给toast提示
                    try {
                        JSONObject object = new JSONObject(throwable.getMessage());
                        String des = object.optString("detail");
                        if (!TextUtils.isEmpty(des)) {
                            ToastUtil.showMessage(des);
                            return;
                        }
                    } catch (JSONException e) {
                        SMSLog.getInstance().w(e);
                    }
                    // / 如果木有找到资源，默认提示
                    int resId = getStringRes(LoginActivity.this, "smssdk_network_error");
                    if (resId > 0) {
                        ToastUtil.showMessage(resId);
                    }
                }
            }
        });
    }
}

