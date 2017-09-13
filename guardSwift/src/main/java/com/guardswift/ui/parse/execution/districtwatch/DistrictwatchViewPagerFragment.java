//package com.guardswift.ui.parse.execution.districtwatch;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//
//import com.google.common.collect.Maps;
//import com.guardswift.R;
//import com.guardswift.persistence.cache.planning.DistrictWatchStartedCache;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
//import com.guardswift.ui.GuardSwiftApplication;
//import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
//
//import java.util.Map;
//
//import javax.inject.Inject;
//
//public class DistrictwatchViewPagerFragment extends AbstractTabsViewPagerFragment {
//
//    protected static final String TAG = DistrictwatchViewPagerFragment.class
//            .getSimpleName();
//
//
//    public static DistrictwatchViewPagerFragment newInstance(Context context, DistrictWatchStarted districtWatchStarted) {
//
//        GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchStartedCache().setSelected(districtWatchStarted);
//
//        DistrictwatchViewPagerFragment fragment = new DistrictwatchViewPagerFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Inject
//    DistrictWatchStartedCache districtWatchStartedCache;
//
//    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();
//
//    public DistrictwatchViewPagerFragment() {
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        fragmentMap = Maps.newLinkedHashMap();
//        fragmentMap.put(getString(R.string.title_tasks_new), ActiveDistrictWatchClientsFragment.newInstance(getContext(), districtWatchStartedCache.getSelected()));
//        fragmentMap.put(getString(R.string.title_tasks_old), FinishedDistrictWatchClientsFragment.newInstance(getContext(), districtWatchStartedCache.getSelected()));
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    protected Map<String, Fragment> getTabbedFragments() {
//        return fragmentMap;
//    }
//
//
//}