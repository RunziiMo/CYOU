package com.runzii.cyou.ui.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.easemob.easeui.controller.EaseUI;
import com.runzii.cyou.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by runzii on 15-9-3.
 */
public abstract class BaseActivity extends SwipeBackActivity {

    @Optional
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected abstract boolean isHideNavigationBar();

    protected abstract boolean isDisplayHomeAsUp();

    protected abstract boolean isEnableSwipe();

    private static Handler sHandler;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        injectViews();
    }

    protected void injectViews() {
        ButterKnife.inject(this);
        setupToolbar();
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    public void setContentViewWithoutInject(int layoutResId) {
        super.setContentView(layoutResId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(isEnableSwipe());

        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        if (isHideNavigationBar()) {
            sHandler = new Handler();

            sHandler.post(mHideRunnable); // hide the navigation bar

            final View decorView = getWindow().getDecorView();

            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    sHandler.post(mHideRunnable); // hide the navigation bar
                }
            });
        }

        if (isDisplayHomeAsUp() && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EaseUI.getInstance().getNotifier().reset();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                hideSoftKeyboard();
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            int flags;
            int curApiVersion = Build.VERSION.SDK_INT;
            // This work only for android 4.4+
            if (curApiVersion >= Build.VERSION_CODES.KITKAT) {
                // This work only for android 4.4+
                // hide navigation bar permanently in android activity
                // touch the screen, the navigation bar will not show
                flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;

            } else {
                // touch the screen, the navigation bar will show
                flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            // must be executed in main thread :)
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    };

    protected abstract int getLayoutId();


    /**
     * 设置ActionBar标题
     *
     * @param text
     */
    public void setActionBarTitle(CharSequence text) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(text);
        }
    }

    /**
     * 设置ActionBar标题
     *
     * @param res
     */
    public void setActionBarTitle(int res) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(res));
        }
    }


    /**
     * 返回ActionBar 标题
     *
     * @return
     */
    public final CharSequence getActionBarTitle() {

        if (getSupportActionBar() != null) {
            return getSupportActionBar().getTitle();
        } else {
            return "";
        }
    }

    public void showActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    public void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * hide inputMethod
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View localView = getCurrentFocus();
            if (localView != null && localView.getWindowToken() != null) {
                IBinder windowToken = localView.getWindowToken();
                inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
            }
        }
    }
}
