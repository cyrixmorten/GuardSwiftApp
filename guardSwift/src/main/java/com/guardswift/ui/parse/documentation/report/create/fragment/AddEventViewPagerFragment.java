package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import com.guardswift.ui.common.UpdateFloatingActionButtonPageChangeListener;
import com.guardswift.ui.parse.documentation.report.create.activity.AbstractCreateEventHandlerActivity;
import com.guardswift.util.ToastHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddEventViewPagerFragment extends InjectingFragment {

    protected static final String TAG = AddEventViewPagerFragment.class.getSimpleName();


    private int PAGES_COUNT = 0;

    public static AddEventViewPagerFragment newInstance(Context context, Client client) {

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

    @Bind(R.id.pager)
    public ViewPager mPager;

    @Bind(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    @Bind(R.id.fab)
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

        ButterKnife.bind(this, rootView);

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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.next, menu);
//        if (isLastPage()) {
//            menu.findItem(R.id.menu_next).setTitle(R.string.action_save);
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_next:
//                if (!isLastPage()) {
//                    nextPageDelayed();
//                } else {
//                    saveEventLog();
//                }
//        }
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if (mPager != null) {
            mPager.removeOnPageChangeListener(mDetailOnPageChangeListener);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((AbstractCreateEventHandlerActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

        // @Override
        // public CharSequence getPageTitle(int clientPosition) {
        // switch (clientPosition) {
        // case 0:
        // return getString(R.string.title_event_type);
        // case 1:
        // return getString(R.string.title_event_amount);
        // case 2:
        // return getString(R.string.title_event_location);
        // case 3:
        // return getString(R.string.title_event_remarks);
        //
        // default:
        // break;
        // }
        // return "";
        // }

        @Override
        public int getCount() {
            // if (eventBundle.getString(TYPE) != null)
            // count++;
            // if (eventBundle.getInt(EXTRA_AMOUNT) > 0)
            // count++;
            // if (eventBundle.getString(EXTRA_LOCATIONS) != null)
            // count++;
            return PAGES_COUNT;
        }


    }

}
