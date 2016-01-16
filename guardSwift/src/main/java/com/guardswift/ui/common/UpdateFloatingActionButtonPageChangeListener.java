package com.guardswift.ui.common;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

/**
 * Created by cyrix on 1/3/16.
 */
public class UpdateFloatingActionButtonPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

    public interface FragmentAdapter {
        Fragment getItem(int position);
    }

    private final Context context;
    private final FragmentAdapter pagerAdapter;
    FloatingActionButton floatingActionButton;

    public UpdateFloatingActionButtonPageChangeListener(Context context, FragmentAdapter pagerAdapter, FloatingActionButton floatingActionButton) {
        this.context = context;
        this.pagerAdapter = pagerAdapter;
        this.floatingActionButton = floatingActionButton;
    }

    @Override
    public void onPageSelected(final int position) {
        floatingActionButton.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                Fragment fragment = pagerAdapter.getItem(position);
                if (fragment instanceof UpdateFloatingActionButton) {
                    ((UpdateFloatingActionButton) fragment).updateFloatingActionButton(context, fab);
                }
            }
        });
        super.onPageSelected(position);
    }
}
