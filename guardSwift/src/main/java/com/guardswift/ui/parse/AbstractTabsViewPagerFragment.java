package com.guardswift.ui.parse;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.ui.common.UpdateFloatingActionButtonPageChangeListener;
import com.guardswift.ui.parse.documentation.report.create.FragmentVisibilityListener;
import com.guardswift.ui.view.slidingtab.SlidingTabLayout;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class AbstractTabsViewPagerFragment extends InjectingFragment {

    protected static final String TAG = AbstractTabsViewPagerFragment.class
            .getSimpleName();

    public AbstractTabsViewPagerFragment() {
    }


    @Bind(R.id.tabs)
    public SlidingTabLayout tabs;
    @Bind(R.id.pager)
    public ViewPager mViewPager;
    @Bind(R.id.coordinator)
    public CoordinatorLayout coordinatorLayout;
    @Bind(R.id.fab)
    public FloatingActionButton floatingActionButton;

    protected abstract Map<String, Fragment> getTabbedFragments();

    private ViewPager.SimpleOnPageChangeListener changeListener;

    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public TasksPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mPagerAdapter = new TasksPagerAdapter(getChildFragmentManager());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.viewpager_slidingtab_fab,
                container, false);

        ButterKnife.bind(this, rootView);

        mViewPager.setAdapter(mPagerAdapter);

        changeListener = new UpdateFloatingActionButtonPageChangeListener(getContext(), mPagerAdapter, floatingActionButton);
        mViewPager.addOnPageChangeListener(changeListener);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Fragment fragment = mPagerAdapter.getItem(mViewPager.getCurrentItem());
                for (Fragment frag : mPagerAdapter.getFragments()) {
                    if (frag != null && frag instanceof FragmentVisibilityListener) {
                        if (frag.equals(fragment)) {
                            Log.w(TAG, "Fragment became visible: " + position);
                            ((FragmentVisibilityListener)frag).fragmentBecameVisible();
                        } else {
                            ((FragmentVisibilityListener)frag).fragmentBecameInvisible();
                        }
                    }
                }
                super.onPageSelected(position);
            }
        });




        // Setting Custom Color for the Scroll bar indicator of the Tab View
//        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//
//            @Override
//            public int getIndicatorColor(int clientPosition) {
//                return getResources().getColor(R.color.tabsScrollColor);
//            }
//        });

//         Setting the ViewPager For the SlidingTabsLayout
        if (getTabbedFragments().keySet().size() > 1) {
            tabs.setDistributeEvenly(true);
            tabs.setViewPager(mViewPager);
        } else {
            tabs.setVisibility(View.GONE);
        }


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changeListener.onPageSelected(0); // init
    }

    private class TasksPagerAdapter extends FragmentStatePagerAdapter implements UpdateFloatingActionButtonPageChangeListener.FragmentAdapter {

        TasksPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public List<Fragment> getFragments() {
            return Lists.newArrayList(getTabbedFragments().values());
        }

        @Override
        public Fragment getItem(int position) {
            return getTabbedFragments().get(getPageTitle(position).toString());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            List<String> titles = Lists.newArrayList(getTabbedFragments().keySet());
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return getTabbedFragments().size();
        }
    }


    @Override
    public void onDestroy() {
        ButterKnife.unbind(this);
        try {
            super.onDestroy();
        } catch (NullPointerException npe) {
            // https://code.google.com/p/android/issues/detail?id=216157
            Log.e(TAG, "NPE: Bug workaround");
        }
    }



}
