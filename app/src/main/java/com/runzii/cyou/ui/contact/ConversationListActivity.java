package com.runzii.cyou.ui.contact;

import android.os.Bundle;

import com.runzii.cyou.R;
import com.runzii.cyou.ui.base.BaseActivity;

/**
 * Created by runzii on 15-10-15.
 */
public class ConversationListActivity extends BaseActivity {
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
    protected int getLayoutId() {
        return R.layout.activity_conversationlist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,new ConversationListFragment()).commit();
    }
}
