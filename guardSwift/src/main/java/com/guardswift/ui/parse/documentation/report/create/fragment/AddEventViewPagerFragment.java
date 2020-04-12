package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.helpers.UpdateFloatingActionButtonPageChangeListener;
import com.guardswift.ui.parse.documentation.report.create.activity.CreateEventHandlerActivity;
import com.guardswift.util.ToastHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddEventViewPagerFragment extends InjectingFragment {

    protected static final String TAG = AddEventViewPagerFragment.class.getSimpleName();


    private int PAGES_COUNT = 0;

    private Unbinder unbinder;

    public static AddEventViewPagerFragment newInstance(Client client) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);

        AddEventViewPagerFragment fragment = new AddEventViewPagerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddEventViewPagerFragment() {
    }

    @Inject
    ClientCache clientCache;
    @Inject
    EventTypeCache eventTypeCache;

    @BindView(R.id.pager)
    public ViewPager mPager;

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public ScreenSlidePagerAdapter mPagerAdapter;

    private DetailOnPageChangeListener mDetailOnPageChangeListener;

    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.viewpager_fab, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mDetailOnPageChangeListener = new DetailOnPageChangeListener(getContext(), mPagerAdapter, floatingActionButton);
        mPager.addOnPageChangeListener(mDetailOnPageChangeListener);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDetailOnPageChangeListener.onPageSelected(0);
    }

    public void nextPageDelayed() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mPager.getCurrentItem() < mPagerAdapter.getCount() - 1) {
                    int page = mPager.getCurrentItem() + 1;
                    mPager.setCurrentItem(positionAfterSkip(page, true));
                    getActivity().invalidateOptionsMenu();
                }
            }
        }, 0);
    }

    public boolean previousPage() {
        Log.d(TAG, "previousPage: " + mPager.getCurrentItem());
        if (mPager.getCurrentItem() > 0) {
            int page = mPager.getCurrentItem() - 1;
            mPager.setCurrentItem(positionAfterSkip(page, false));
            getActivity().invalidateOptionsMenu();
            return true;
        }
        return false;
    }

    private int positionAfterSkip(int page, boolean forward) {
        EventType eventType = eventTypeCache.getSelected();
        if (eventType == null) {
            ToastHelper.toast(getContext(), getString(R.string.please_select_event_type));
            return ScreenSlidePagerAdapter.ADD_TYPE;
        }
        if (page == ScreenSlidePagerAdapter.ADD_PEOPLE && !eventType.hasPeople()) {
            page = incDec(forward, page);
        }
        if (page == ScreenSlidePagerAdapter.ADD_LOCATIONS && !eventType.hasLocations()) {
            page = incDec(forward, page);
        }
        if (page == ScreenSlidePagerAdapter.ADD_REMARKS && !eventType.hasRemarks()) {
            page = incDec(forward, page);
        }
        return page;
    }

    private int incDec(boolean inc, int value) {
        if (inc)
            return ++value;
        return --value;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mPager != null) {
            mPager.removeOnPageChangeListener(mDetailOnPageChangeListener);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((CreateEventHandlerActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(getActivity() instanceof ViewPager.OnPageChangeListener)) {
            throw new IllegalStateException(
                    "Activity must be an instance of OnPageChangeListener!");
        }
    }


    private boolean lastPage = false;

    public void setPage(int page) {
        if (mPager != null) {
            mPager.setCurrentItem(page);
        }
    }

    public void updateVisibilityCurrentPage() {
        if (mPager != null && mPagerAdapter != null) {
            updateVisibility(mPagerAdapter.getItem(mPager.getCurrentItem()));
        }
    }
    private void updateVisibility(Fragment fragment) {
        for (Fragment frag : mPagerAdapter.getFragments()) {
            if (frag != null && frag instanceof EventEntryFragment) {
                EventEntryFragment vFragment = ((EventEntryFragment) frag);
                if (frag.equals(fragment)) {
                    Log.w(TAG, "Fragment became visible: " + vFragment.getTitle());
                    vFragment.fragmentBecameVisible();
                    if (mActionBar != null) {
                        Log.w(TAG, "Set title");
                        mActionBar.setTitle(vFragment.getTitle());
                    }
                } else {
                    vFragment.fragmentBecameInvisible();
                }
            }
        }
    }

    public class DetailOnPageChangeListener extends
            UpdateFloatingActionButtonPageChangeListener {

        public DetailOnPageChangeListener(Context context, FragmentAdapter pagerAdapter, FloatingActionButton floatingActionButton) {
            super(context, pagerAdapter, floatingActionButton);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            final Fragment fragment = mPagerAdapter.getItem(position);
            updateVisibility(fragment);

            lastPage = position == (PAGES_COUNT - 1);

            ((ViewPager.OnPageChangeListener) getActivity()).onPageSelected(position);
        }

    }

    public boolean isLastPage() {
        return lastPage;
    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter implements UpdateFloatingActionButtonPageChangeListener.FragmentAdapter {

        public static final int ADD_TYPE = 0;
        public static final int ADD_PEOPLE = 1;
        public static final int ADD_LOCATIONS = 2;
        public static final int ADD_REMARKS = 3;
        public static final int ADD_SUMMARY = 4;

        List<Fragment> fragments = new ArrayList<Fragment>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.add(AddEventTypeFragment.newInstance(clientCache.getSelected()));
//            fragments.add(AddEventPeopleFragment.newInstance(clientCache.getSelected()));
//            fragments.add(AddEventLocationFragment.newInstance(clientCache.getSelected()));
//            fragments.add(AddEventRemarkFragment.newInstance(clientCache.getSelected(), eventTypeCache.getSelected()));
            fragments.add(AddEventSummaryFragment.newInstance());

            PAGES_COUNT = fragments.size();
        }


        public List<Fragment> getFragments() {
            return fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }


        @Override
        public int getCount() {
            return PAGES_COUNT;
        }


    }

}
