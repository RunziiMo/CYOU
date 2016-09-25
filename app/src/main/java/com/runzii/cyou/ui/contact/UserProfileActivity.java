package com.runzii.cyou.ui.contact;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.utils.EaseUserUtils;
import com.easemob.easeui.widget.CircleTransformation;
import com.runzii.cyou.R;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.common.utils.ToastUtil;
import com.runzii.cyou.common.view.RevealBackgroundView;
import com.runzii.cyou.ui.base.BaseActivity;
import com.runzii.cyou.ui.call.VideoCallActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import butterknife.InjectView;

/**
 * Created by Miroslaw Stanek on 14.01.15.
 */
public class UserProfileActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener, View.OnClickListener {
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;


    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    @InjectView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @InjectView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;
    @InjectView(R.id.user_username)
    TextView tvUsername;
    @InjectView(R.id.user_nickname)
    TextView tvNickName;
    @InjectView(R.id.tv_signature)
    TextView tvSignature;

    @InjectView(R.id.tlUserProfileTabs)
    TabLayout tlUserProfileTabs;

    @InjectView(R.id.ivUserProfilePhoto)
    ImageView ivUserProfilePhoto;
    @InjectView(R.id.vUserDetails)
    View vUserDetails;
    @InjectView(R.id.btnFollow)
    Button btnFollow;
    @InjectView(R.id.vUserStats)
    View vUserStats;
    @InjectView(R.id.vUserProfileRoot)
    View vUserProfileRoot;

    private UserProfileAdapter userPhotosAdapter;


    @Override
    protected boolean isHideNavigationBar() {
        return false;
    }

    @Override
    protected boolean isDisplayHomeAsUp() {
        return true;
    }

    @Override
    protected boolean isEnableSwipe() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupListener();
        setupTabs();
        setupUserProfileGrid();
        setupRevealBackground(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra("setting", false)) {
            getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_user_profile_other, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {

            return true;
        } else if (id == R.id.action_add) {
            addContact(getIntent().getStringExtra("username"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 添加contact
     */
    public void addContact(final String username) {
        if (EMChatManager.getInstance().getCurrentUser().equals(username)) {
            new MaterialDialog.Builder(this)
                    .content(R.string.not_add_myself).show();
            return;
        }

        if (DemoHelper.getInstance().getContactList().containsKey(username)) {
            //提示已在好友列表中(在黑名单列表里)，无需添加
            if (EMContactManager.getInstance().getBlackListUsernames().contains(username)) {
                new MaterialDialog.Builder(this)
                        .content(R.string.user_already_in_contactlist).show();
                return;
            }
            new MaterialDialog.Builder(this)
                    .content(R.string.This_user_is_already_your_friend).show();
            return;
        }

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(R.string.Is_sending_a_request)
                .cancelable(false)
                .show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo写死了个reason，实际应该让用户手动填入
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMContactManager.getInstance().addContact(username, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            ToastUtil.showMessage(s1);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            ToastUtil.showMessage(s2 + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void setupListener() {
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        boolean enableUpdate = intent.getBooleanExtra("setting", false);
        if (enableUpdate) {
            ivUserProfilePhoto.setOnClickListener(this);
            tvNickName.setOnClickListener(this);
            tvSignature.setOnClickListener(this);
        }

        if (username != null) {
            if (username.equals(EMChatManager.getInstance().getCurrentUser())) {
                tvUsername.setText(EMChatManager.getInstance().getCurrentUser());
                EaseUserUtils.setUserNick(username, tvNickName);
                EaseUserUtils.setUserAvatar(this, username, ivUserProfilePhoto);
                EaseUserUtils.setUserSign(username, tvSignature);
            } else {
                tvUsername.setText(username);
                EaseUserUtils.setUserNick(username, tvNickName);
                EaseUserUtils.setUserAvatar(this, username, ivUserProfilePhoto);
                EaseUserUtils.setUserSign(username, tvSignature);
                asyncFetchUserInfo(username);
            }

        }

        btnFollow.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_profile;
    }

    public void asyncFetchUserInfo(String username) {
        DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    tvNickName.setText(user.getNick());
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Picasso.with(UserProfileActivity.this).load(user.getAvatar()).transform(new CircleTransformation()).placeholder(R.drawable.em_default_avatar).into(ivUserProfilePhoto);
                    } else {
                        Picasso.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).transform(new CircleTransformation()).into(ivUserProfilePhoto);
                    }
                    DemoHelper.getInstance().saveContact(user);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    private void setupTabs() {
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_call_white_24dp));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_local_pizza_white_24dp));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_edit_white_24dp));
        tlUserProfileTabs.addTab(tlUserProfileTabs.newTab().setIcon(R.drawable.ic_person_add_white_24dp));
    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
        rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                userPhotosAdapter.setLockedAnimations(true);
            }
        });
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
            userPhotosAdapter.setLockedAnimations(true);
        }
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            tlUserProfileTabs.setVisibility(View.VISIBLE);
            vUserProfileRoot.setVisibility(View.VISIBLE);
            userPhotosAdapter = new UserProfileAdapter(this);
            rvUserProfile.setAdapter(userPhotosAdapter);
            animateUserProfileOptions();
            animateUserProfileHeader();
        } else {
            tlUserProfileTabs.setVisibility(View.INVISIBLE);
            rvUserProfile.setVisibility(View.INVISIBLE);
            vUserProfileRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        ivUserProfilePhoto.setTranslationY(-ivUserProfilePhoto.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());
        vUserStats.setAlpha(0);

        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        ivUserProfilePhoto.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivUserProfilePhoto:
                uploadHeadPhoto();
                break;
            case R.id.user_nickname:
                new MaterialDialog.Builder(this)
                        .title(R.string.setting_nickname)
                        .content("请输入昵称")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("随便填", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

                                if (TextUtils.isEmpty(charSequence)) {
                                    ToastUtil.showMessage(R.string.toast_nick_not_isnull);
                                    return;
                                }
                                updateRemoteNick(charSequence.toString());
                            }
                        })
                        .show();
                break;
            case R.id.tv_signature:
                new MaterialDialog.Builder(this)
                        .title(R.string.setting_signature)
                        .content("请输入签名")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("随便填", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

                                if (TextUtils.isEmpty(charSequence)) {
                                    ToastUtil.showMessage(R.string.toast_sign_not_isnull);
                                    return;
                                }
                                updateRemoteSign(charSequence.toString());
                            }
                        })
                        .show();
                break;
            case R.id.btnFollow:
                String username = getIntent().getStringExtra("username");
                if (username.endsWith(EMChatManager.getInstance().getCurrentUser())) {
                    ToastUtil.showMessage("你不能和自己视频");
                } else {
                    startActivity(new Intent(UserProfileActivity.this, VideoCallActivity.class).putExtra("username", username)
                            .putExtra("isComingCall", false));
                }
                break;
        }

    }

    private void updateRemoteNick(final String nickName) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dl_update_nick)
                .content(R.string.dl_waiting)
                .progress(true, -1)
                .show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updatenick = DemoHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
                if (UserProfileActivity.this.isFinishing()) {
                    return;
                }
                if (!updatenick) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ToastUtil.showMessage(R.string.toast_updatenick_fail);
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtil.showMessage(R.string.toast_updatenick_success);
                            tvNickName.setText(nickName);
                        }
                    });
                }
            }
        }).start();
    }

    private void updateRemoteSign(final String sign) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dl_update_sign)
                .content(R.string.dl_waiting)
                .progress(true, -1)
                .show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updatesign = DemoHelper.getInstance().getUserProfileManager().updateCurrentUserSignature(sign);
                if (UserProfileActivity.this.isFinishing()) {
                    return;
                }
                if (!updatesign) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ToastUtil.showMessage(R.string.toast_updatesign_fail);
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtil.showMessage(R.string.toast_updatesign_success);
                            tvSignature.setText(sign);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            uploadUserAvatar(Bitmap2Bytes(photo));
        }

    }

    private void uploadUserAvatar(final byte[] data) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dl_update_photo)
                .content(R.string.dl_waiting)
                .progress(true, -1)
                .show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = DemoHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Picasso.with(UserProfileActivity.this).load(avatarUrl).transform(new CircleTransformation()).placeholder(R.drawable.responsible).into(ivUserProfilePhoto);
                            ToastUtil.showMessage(R.string.toast_updatephoto_success);
                        } else {
                            ToastUtil.showMessage(R.string.toast_updatephoto_fail);
                        }

                    }
                });

            }
        }).start();
    }

    private void uploadHeadPhoto() {
        new MaterialDialog.Builder(this).title(R.string.dl_title_upload_photo)
                .items(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)})
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                ToastUtil.showMessage(R.string.toast_no_support);
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
