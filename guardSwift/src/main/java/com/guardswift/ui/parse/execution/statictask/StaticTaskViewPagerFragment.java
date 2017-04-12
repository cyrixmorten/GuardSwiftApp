package com.guardswift.ui.parse.execution.statictask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;

import javax.inject.Inject;

public class StaticTaskViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = StaticTaskViewPagerFragment.class
            .getSimpleName();

//    private static final String CREATE_REPORT = "com.guardswift.CREATE_REPORT";
    private static final int CLIENT_SELECTION = 1;

    public static StaticTaskViewPagerFragment newCreateReportInstance() {
        return newInstance(true);
    }

    public static StaticTaskViewPagerFragment newInstance() {
        return newInstance(false);
    }

    private static StaticTaskViewPagerFragment newInstance(boolean createReport) {
        StaticTaskViewPagerFragment fragment = new StaticTaskViewPagerFragment();

        Bundle args = new Bundle();
//        args.putBoolean(CREATE_REPORT, createReport);
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    ClientCache clientCache;
    @Inject
    GuardCache guardCache;



    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public StaticTaskViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_pending), PendingStaticTasksFragment.newInstance());
        fragmentMap.put(getString(R.string.title_active), ActiveStaticTasksFragment.newInstance());
        fragmentMap.put(getString(R.string.title_finished), FinishedStaticTasksFragment.newInstance());

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        if (getArguments().getBoolean(CREATE_REPORT)) {
//            ClientListFragment clientSelection = ClientListFragment.newInstance();
//            clientSelection.setTargetFragment(this, CLIENT_SELECTION);
////            if (getActivity() != null && getActivity() instanceof MainNavigationDrawer.FragmentDrawerCallback) {
////                ((MainActivity)getActivity()).selectItem(, R.string.static_guarding_reports);
//            getChildFragmentManager().beginTransaction().replace(R.id.content, clientSelection).addToBackStack(null).commit();
////            }
//
//        }
    }


    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

