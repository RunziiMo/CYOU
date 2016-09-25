package com.runzii.cyou.ui.init;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.runzii.cyou.R;
import com.runzii.cyou.common.view.CircleIndicator;
import com.runzii.cyou.ui.base.BaseActivity;
import com.runzii.cyou.ui.base.BaseFragment;

import butterknife.InjectView;

/**
 * 开屏页
 */
public class IntroduceActivity extends BaseActivity {

    @InjectView(R.id.viewpager_introduce)
    ViewPager viewPager;

    @InjectView(R.id.indicator_introduce)
    CircleIndicator circleIndicator;

    @InjectView(R.id.btn_introduce)
    Button btn_introduce;

    private static final int sleepTime = 3000;

    private static int[] introduceBackRes = {R.drawable.first_user_1, R.drawable.first_user_2
            , R.drawable.first_user_3, R.drawable.first_user_4};

    @Override
    protected boolean isHideNavigationBar() {
        return true;
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
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        viewPager.setAdapter(new IntroduceAdapter(getSupportFragmentManager()));
        circleIndicator.setViewPager(viewPager);
        circleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == introduceBackRes.length - 1) {
                    btn_introduce.setVisibility(View.VISIBLE);
                } else {
                    btn_introduce.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btn_introduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroduceActivity.this, SplashActivity.class));
                finish();
            }
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_introduce;
    }

    private class IntroduceAdapter extends FragmentPagerAdapter {

        public IntroduceAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return introduceBackRes.length;
        }

        @Override
        public Fragment getItem(int position) {
            return IntroduceFragment.newInstance(introduceBackRes[position]);
        }

    }

    public static class IntroduceFragment extends BaseFragment {


        public static String image = "image";
        private int imageRes;

        public static IntroduceFragment newInstance(int res) {
            IntroduceFragment fragment = new IntroduceFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(image, res);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            imageRes = getArguments().getInt(image);
        }

        @Override
        protected int getLayoutId() {
            return R.layout.item_introduce;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((ImageView) findViewById(R.id.iv_introduce)).setImageResource(imageRes);

        }
    }


}
