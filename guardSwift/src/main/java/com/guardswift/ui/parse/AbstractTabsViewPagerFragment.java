package com.guardswift.ui.parse;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.ui.common.UpdateFloatingActionButtonPageChangeListener;
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


        tabs.setDistributeEvenly(true);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
//        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//
//            @Override
//            public int getIndicatorColor(int position) {
//                return getResources().getColor(R.color.tabsScrollColor);
//            }
//        });

//         Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(mViewPager);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changeListener.onPageSelected(0); // init
    }

    private class TasksPagerAdapter extends FragmentStatePagerAdapter implements UpdateFloatingActionButtonPageChangeListener.FragmentAdapter {

        public TasksPagerAdapter(FragmentManager fm) {
            super(fm);
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
        super.onDestroy();
    }


//    public void onEventMainThread(RestartCurrentCircuit ev) {
////        final Circuit circuit = Circuit.Recent.getSelected();
////        final Circuit circuit = circuitCache.get(ParseCache.Circuit.SELECTED);
//        final CircuitStarted circuitStarted = circuitStartedCache.getSelected();
//        if (circuitStarted != null) {
//            new MaterialDialog.Builder(getActivity())
//                    .title(getString(R.string.title_reset_circuit, circuitStarted.getName()))
//                    .positiveText(android.R.string.ok)
//                    .negativeText(android.R.string.cancel)
//                    .content(R.string.message_reset_circuit)
//                    .callback(new MaterialDialog.ButtonCallback() {
//                                  @Override
//                                  public void onPositive(MaterialDialog dialog) {
//                                      restartCircuit(circuitStarted);
//                                  }
//                              }
//                    ).show();
//        }
//    }

    // TODO should call Cloud Function
//    private void restartCircuit(final CircuitStarted circuitStarted) {
//        final ProgressDialog progress = new ProgressDialog(
//                getActivity());
//        progress.setIndeterminate(true);
//        progress.setCancelable(false);
//        progress.setMessage(getString(R.string.working));
//        progress.show();
//
//        new CircuitUnit.QueryBuilder(true)
//                .matching(circuitStarted.getCircuit())
////                .isRunToday()
//                .buildNoIncludes()
//                .findInBackground(
//                        new FindCallback<CircuitUnit>() {
//
//                            @Override
//                            public void done(
//                                    final List<CircuitUnit> objects,
//                                    ParseException e) {
//                                if (e != null) {
//                                    new HandleException(getActivity(), TAG, "Reset Circuit" + circuitStarted.getName(), e);
//                                    return;
//                                }
//
//                                for (CircuitUnit circuitUnit : objects) {
//
//                                    circuitUnit.reset();
//
////                                    circuitUnit.getAutomationStrategy().clearAllAutomaticReports();
//                                }
//
//                                ParseObject.pinAllInBackground(CircuitUnit.PIN, objects, new SaveCallback() {
//                                    @Override
//                                    public void done(ParseException e) {
//                                        if (e != null) {
//                                            new HandleException(getActivity(), TAG, " restartCircuit pin", e);
//                                        }
//
//                                        progress.dismiss();
//
//                                        EventBusController.postUIUpdate(objects);
//
////                                        geofencingModule.rebuildGeofences(new CircuitUnit(), TAG);
//
//                                        ParseObject.saveAllInBackground(objects, new SaveCallback() {
//                                            @Override
//                                            public void done(ParseException e) {
//                                                if (e != null) {
//                                                    new HandleException(getActivity(), TAG, " restartCircuit saveAllInBackground", e);
//                                                }
//                                            }
//                                        });
//                                    }
//                                });
//
//
//
//                            }
//                        });
//    }

}
