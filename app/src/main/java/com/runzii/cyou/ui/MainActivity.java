package com.runzii.cyou.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.easemob.EMCallBack;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.EMValueCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chatuidemo.Constant;
import com.easemob.chatuidemo.db.InviteMessgeDao;
import com.easemob.chatuidemo.db.UserDao;
import com.easemob.easeui.domain.EaseUser;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.ToggleDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseUser;
import com.runzii.cyou.R;
import com.runzii.cyou.common.utils.CameraUtil;
import com.runzii.cyou.common.utils.DemoHelper;
import com.runzii.cyou.common.utils.LogUtil;
import com.runzii.cyou.common.utils.RandomVideoUtil;
import com.runzii.cyou.common.utils.ToastUtil;
import com.runzii.cyou.ui.base.BaseActivity;
import com.runzii.cyou.ui.call.VideoCallActivity;
import com.runzii.cyou.ui.contact.ContactListActivity;
import com.runzii.cyou.ui.contact.ConversationListActivity;
import com.runzii.cyou.ui.contact.NewFriendsMsgActivity;
import com.runzii.cyou.ui.contact.UserProfileActivity;
import com.runzii.cyou.ui.init.LoginActivity;

import java.util.Random;

public class MainActivity extends BaseActivity implements EMEventListener, View.OnClickListener, OnCheckedChangeListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int[] backgrounds = {R.drawable.bamboo,
            R.drawable.mat2, R.drawable.mat3, R.drawable.ny_light, R.drawable.material_drawer_header};


    private SurfaceView localSurface;
    private SurfaceHolder localSurfaceHolder;
    private CameraUtil cameraHelper;
    private View blur_view;

    FloatingActionButton startRandomChat, startVideoChat;
    FloatingActionsMenu showFAB;


    // 账号在别处登录
    public boolean isConflict = false;
    // 账号被移除
    private boolean isCurrentAccountRemoved = false;

    /**
     * 检查当前用户是否被删除
     */
    public boolean getCurrentAccountRemoved() {
        return isCurrentAccountRemoved;
    }

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

        if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED, false)) {
            // 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            DemoHelper.getInstance().logout(true, null);
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false)) {
            // 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }


        if (getIntent().getBooleanExtra(Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (getIntent().getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }

        inviteMessgeDao = new InviteMessgeDao(this);
        userDao = new UserDao(this);

        initLauncherUIView(savedInstanceState);

        // 注册群组和联系人监听
        DemoHelper.getInstance().registerGroupAndContactListener();

        registerBroadcastReceiver();


    }

    private boolean isConflictDialogShow;
    private boolean isAccountRemovedDialogShow;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager broadcastManager;

    /**
     * 显示帐号在别处登录dialog
     */
    private void showConflictDialog() {
        isConflictDialogShow = true;
        DemoHelper.getInstance().logout(false, null);
        String st = getResources().getString(R.string.Logoff_notification);
        if (!MainActivity.this.isFinishing()) {
            new MaterialDialog.Builder(this).title(st)
                    .content(R.string.connect_conflict)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                    }).show();
            isConflict = true;
        }

    }

    /**
     * 帐号被移除的dialog
     */
    private void showAccountRemovedDialog() {
        isAccountRemovedDialogShow = true;
        DemoHelper.getInstance().logout(true, null);
        String st5 = getResources().getString(R.string.Remove_the_notification);
        if (!MainActivity.this.isFinishing()) {
            new MaterialDialog.Builder(this).title(st5)
                    .content("账号不存在或被删除")
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            finish();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                    }).show();
            isCurrentAccountRemoved = true;
        }

    }


    private AccountHeader headerResult = null;
    private Drawer result = null;

    private IProfile profile = null;

    /**
     * 初始化主界面UI视图
     */
    private void initLauncherUIView(Bundle savedInstanceState) {

        localSurface = (SurfaceView) findViewById(R.id.local_surface);
        localSurfaceHolder = localSurface.getHolder();

        cameraHelper = new CameraUtil(localSurfaceHolder);

        localSurfaceHolder.addCallback(new LocalCallback());

        profile = new ProfileDrawerItem()
                .withName(EMChatManager.getInstance().getCurrentUser())
                .withEmail(DemoHelper.getInstance().getUserProfileManager().getCurrentUserInfo().getSingature())
                .withIcon(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.responsible)).withIdentifier(100);
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(backgrounds[new Random().nextInt(backgrounds.length)])
                .withSelectionListEnabledForSingleProfile(true)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        startUserProfile(view);
                        return false;
                    }
                })
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(getToolbar())
                .withTranslucentStatusBar(true)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_friends).withDescription(R.string.drawer_item_friends_description).withIcon(GoogleMaterial.Icon.gmd_face).withIdentifier(4).withSelectable(false).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_conversation).withDescription(R.string.drawer_item_conversation_description).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(6).withSelectable(false).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_noti).withDescription(R.string.drawer_item_noti_description).withIcon(GoogleMaterial.Icon.gmd_message).withIdentifier(5).withSelectable(false).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)),
                        new SectionDrawerItem().withName("设置"),
                        new DividerDrawerItem(),
                        new SwitchDrawerItem().withName("只匹配同城").withIcon(GoogleMaterial.Icon.gmd_settings).withChecked(true).withOnCheckedChangeListener(this).withSelectable(false),
                        new SwitchDrawerItem().withName("只匹配异性").withIcon(GoogleMaterial.Icon.gmd_settings).withChecked(true).withOnCheckedChangeListener(this).withSelectable(false),
                        new ToggleDrawerItem().withName("新消息声音").withIcon(GoogleMaterial.Icon.gmd_settings).withChecked(true).withOnCheckedChangeListener(this).withSelectable(false),
                        new DividerDrawerItem()
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(final View v, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Intent intent = null;
                            switch (drawerItem.getIdentifier()) {
                                case 4:
                                    intent = new Intent(MainActivity.this, ContactListActivity.class);
                                    break;
                                case 5:
                                    intent = new Intent(MainActivity.this, NewFriendsMsgActivity.class);
                                    break;
                                case 6:
                                    intent = new Intent(MainActivity.this, ConversationListActivity.class);
                                    break;

                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .withSelectedItem(-1)
                .build();


        startVideoChat = (FloatingActionButton) findViewById(R.id.action_a);
        startVideoChat.setOnClickListener(this);
        startRandomChat = (FloatingActionButton) findViewById(R.id.action_b);
        startRandomChat.setOnClickListener(this);
        showFAB = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        showFAB.setOnClickListener(this);
        blur_view = findViewById(R.id.blur_view);
        final AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        final AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(150);
//        showFAB.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
//
//            @Override
//            public void onMenuExpanded() {
//                blur_view.startAnimation(animation);
//            }
//
//            @Override
//            public void onMenuCollapsed() {
//                blur_view.startAnimation(animation1);
//            }
//        });

    }

    private void startUserProfile(final View v) {
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class).putExtra("setting", true)
                .putExtra("username", EMChatManager.getInstance().getCurrentUser());
        intent.putExtra(UserProfileActivity.ARG_REVEAL_START_LOCATION, startingLocation);
        overridePendingTransition(0, 0);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void logout() {

        final MaterialDialog pd = new MaterialDialog.Builder(this)
                .content(R.string.Are_logged_out)
                .cancelable(false)
                .show();
        DemoHelper.getInstance().logout(false, new EMCallBack() {

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));

                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        pd.dismiss();
                        ToastUtil.showMessage("unbind devicetokens failed");

                    }
                });
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void onResume() {
        LogUtil.i(LogUtil.getLogUtilsTag(MainActivity.class), "onResume start");
        super.onResume();

        if (cameraHelper != null) {
            cameraHelper.startCapture();
        }

        if (!isConflict && !isCurrentAccountRemoved) {
            updateUnreadLabel();
            updateUnreadAddressLable();
            asyncFetchUserInfo();
        }

        // unregister this event listener when this activity enters the
        // background
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.pushActivity(this);

        // register the event listener when enter the foreground
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage, EMNotifierEvent.Event.EventConversationListChanged});
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraHelper != null) {
            cameraHelper.stopCapture();
        }

    }

    @Override
    protected void onStop() {
        EMChatManager.getInstance().unregisterEventListener(this);
        DemoHelper sdkHelper = DemoHelper.getInstance();
        sdkHelper.popActivity(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            cameraHelper.stopCapture();
            cameraHelper = null;
        } catch (Exception e) {
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isConflict", isConflict);
        outState.putBoolean(Constant.ACCOUNT_REMOVED, isCurrentAccountRemoved);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void registerBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_CONTACT_CHANAGED);
        intentFilter.addAction(Constant.ACTION_GROUP_CHANAGED);
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUnreadLabel();
                updateUnreadAddressLable();
                String action = intent.getAction();
                if (action.equals(Constant.ACTION_GROUP_CHANAGED)) {
//                    if (EaseCommonUtils.getTopActivity(MainActivity.this).equals(GroupsActivity.class.getName())) {
//                        GroupsActivity.instance.onResume();
//                    }
                }
            }
        };
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    public void asyncFetchUserInfo() {
        DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(EMChatManager.getInstance().getCurrentUser(), new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(final EaseUser user) {
                if (user != null) {
                    try {
                        if (!TextUtils.isEmpty(user.getAvatar())) {
                            ImageLoader.getInstance().loadImage(user.getAvatar(), new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {

                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {

                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    profile.withIcon(bitmap);
                                    profile.withName(user.getNick());
                                    profile.withEmail(user.getSingature());
                                    headerResult.updateProfile(profile);
                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                }
                            });
//                            Picasso.with(MainActivity.this).load(user.getAvatar()).placeholder(R.drawable.responsible).get();

                        } else {
//                            Picasso.with(MainActivity.this).load(R.drawable.responsible).get();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    DemoHelper.getInstance().saveContact(user);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }

    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            result.updateBadge(4, new StringHolder(count + ""));
        }
    }

    /**
     * 刷新申请与通知消息数
     */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
                if (count > 0) {
                    result.updateBadge(4, new StringHolder(count + ""));
                }
            }
        });

    }

    /**
     * 获取未读申请与通知消息
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        unreadAddressCountTotal = inviteMessgeDao.getUnreadMessagesCount();
        return unreadAddressCountTotal;
    }

    /**
     * 获取未读消息数
     *
     * @return
     */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        int chatroomUnreadMsgCount = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        for (EMConversation conversation : EMChatManager.getInstance().getAllConversations().values()) {
            if (conversation.getType() == EMConversation.EMConversationType.ChatRoom)
                chatroomUnreadMsgCount = chatroomUnreadMsgCount + conversation.getUnreadMsgCount();
        }
        return unreadMsgCountTotal - chatroomUnreadMsgCount;
    }

    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;


    @Override
    public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.action_b:
                RandomVideoUtil.randomVideo(this);
                break;
            case R.id.action_a:
                new MaterialDialog.Builder(this)
                        .title(R.string.http_norandomvideo)
                        .content("请输入对方的账号")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("随便填", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                startActivity(new Intent(MainActivity.this, VideoCallActivity.class).putExtra("username", charSequence.toString())
                                        .putExtra("isComingCall", false));
                            }
                        })
                        .show();
                break;
            case R.id.multiple_actions:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(Constant.ACCOUNT_CONFLICT, false) && !isConflictDialogShow) {
            showConflictDialog();
        } else if (intent.getBooleanExtra(Constant.ACCOUNT_REMOVED, false) && !isAccountRemovedDialogShow) {
            showAccountRemovedDialog();
        }
    }

    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: // 普通消息
            {
                EMMessage message = (EMMessage) event.getData();
                // 提示新消息
                DemoHelper.getInstance().getNotifier().onNewMsg(message);

                refreshUIWithMessage();
                break;
            }

            case EventOfflineMessage: {
                refreshUIWithMessage();
                break;
            }

            case EventConversationListChanged: {
                refreshUIWithMessage();
                break;
            }

            default:
                break;
        }
    }

    private void refreshUIWithMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                // 刷新bottom bar消息未读数
                updateUnreadLabel();
            }
        });
    }


    /**
     * 本地SurfaceHolder callback
     */
    class LocalCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            cameraHelper.startCapture(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

}
