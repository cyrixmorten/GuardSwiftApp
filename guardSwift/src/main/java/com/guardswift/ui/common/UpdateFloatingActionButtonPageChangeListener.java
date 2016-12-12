package com.guardswift.ui.common;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;


public class UpdateFloatingActionButtonPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

    private static String TAG = "FABChangeListener";

    public interface FragmentAdapter {
        Fragment getItem(int position);
    }

    private final Context context;
    private final FragmentAdapter pagerAdapter;
    private FloatingActionButton floatingActionButton;

    public UpdateFloatingActionButtonPageChangeListener(Context context, FragmentAdapter pagerAdapter, FloatingActionButton floatingActionButton) {
        this.context = context;
        this.pagerAdapter = pagerAdapter;
        this.floatingActionButton = floatingActionButton;
    }

    @Override
    public void onPageSelected(final int position) {

        Fragment fragment = pagerAdapter.getItem(position);

        // http://stackoverflow.com/questions/31046469/floatingactionbutton-setvisibility-not-working
        floatingActionButton.setVisibility(View.GONE);

        if (fragment instanceof UpdateFloatingActionButton) {
            ((UpdateFloatingActionButton) fragment).updateFloatingActionButton(context, floatingActionButton);
        }


        super.onPageSelected(position);
    }
}
