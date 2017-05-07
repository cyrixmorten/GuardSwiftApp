//package com.guardswift.ui.fragments.task.districtwatch;
//
//import android.app.Activity;
//import android.location.Location;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.guardswift.R;
//import com.guardswift.ui.activity.MainActivity;
//import com.guardswift.ui.adapters.parse.DistrictWatchClientAdapter;
//import com.guardswift.core.tasks.controller.DistrictWatchClientController;
//import com.guardswift.core.tasks.controller.TaskController;
//import com.guardswift.dagger.InjectingListFragment;
//import com.guardswift.eventbus.EventBusController;
//import com.guardswift.eventbus.events.UpdateUIEvent;
//import com.guardswift.core.exceptions.HandleException;
//import com.guardswift.persitence.cache.DistrictWatchStartedCache;
//import com.guardswift.persistence.parse.tasklist.DistrictWatch;
//import com.guardswift.persistence.parse.tasklist.DistrictWatchStarted;
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.planning.districtwatch.DistrictWatchClient;
//import com.parse.ParseException;
//import com.parse.ParseQuery;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//
////import com.guardswift.modules.LocationsModule;
//
//public abstract class AbstractDistrictWatchClientsFragment extends InjectingListFragment
//		implements OnRefreshListener {
//
//	protected static final String TAG = AbstractDistrictWatchClientsFragment.class
//			.getSimpleName();
//
//    public abstract void doneUpdatingObjects();
//
//    public abstract boolean getShowOnlyVisited();
//
//    @Inject
//    DistrictWatchClientController controller;
//	@Inject
//	DistrictWatchStartedCache districtWatchStartedCache;
//
//	private View mRootView;
//	private DistrictWatchClientAdapter mAdapter;
//	private boolean only_show_visited;
//
//	private MainActivity activity;
//
//	// private List<DistrictWatch> mDistrictWatches;
//	// private DistrictWatch mSelectedDistrictWatch;
//
//
//	public AbstractDistrictWatchClientsFragment() {
//	}
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		// setRetainInstance(true);
//		setHasOptionsMenu(true);
//
//	}
//
//	@Override
//	public void setUserVisibleHint(boolean isVisibleToUser) {
//		if (!isVisibleToUser) {
//			setMenuVisibility(false);
//		}
//	};
//
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		super.onCreateOptionsMenu(menu, inflater);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
////		case R.id.menu_sort_nearest:
////			mAdapter.setSortBy(DistrictWatchClient.SORTBY_NEAREST);
////			break;
////		case R.id.menu_sort_address:
////			mAdapter.setSortBy(DistrictWatchClient.SORTBY_ADDRESS);
////			break;
////
////		case R.id.menu_refresh:
////
////			refreshParseData();
////
////			return true;
//		default:
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void refreshParseData() {
//
//		DistrictWatchStarted districtWatchStarted = districtWatchStartedCache.getSelected();
//		DistrictWatch districtWatch = districtWatchStarted.getDistrictWatch();
//
//        Log.d(TAG, "refreshParseData " + districtWatch);
//
//		if (districtWatch == null) {
//            mSwipeRefresher.setRefreshing(false);
//            Toast.makeText(getActivity(), getString(R.string.error_loading), Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                mSwipeRefresher.setRefreshing(true);
//            }
//        }, 200);
//
//		ParseQuery<DistrictWatchClient> query = new DistrictWatchClient()
//				.getQueryBuilder(false).matching(districtWatch)
//				.build();
//		new DistrictWatchClient().updateAll(query,
//				new ExtendedParseObject.DataStoreCallback<DistrictWatchClient>() {
//
//					@Override
//					public void success(List<DistrictWatchClient> objects) {
//
//						clearLoadIndicators();
//
//                        doneUpdatingObjects();
////                        for (DistrictWatchClient client: objects) {
////                            Log.d(TAG, client.getFullAddress());
////                        }
//						EventBusController.postUIUpdate(objects);
//
//					}
//
//					@Override
//					public void failed(ParseException e) {
//						// progress.dismiss();
//						Toast.makeText(
//								getActivity(),
//								"Synkronisering af områdevagt fejlede "
//										+ e.getMessage(), Toast.LENGTH_SHORT)
//								.show();
//
//						clearLoadIndicators();
//
//						new HandleException(getActivity(), TAG, "Sync district watches", e);
//					}
//				});
//	}
//
//	private void clearLoadIndicators() {
//		if (mSwipeRefresher != null)
//			mSwipeRefresher.setRefreshing(false);
//		// if (mLoading != null && mLoading.getVisibility() == View.VISIBLE) {
//		// new Handler().postDelayed(new Runnable() {
//		//
//		// @Override
//		// public void run() {
//		// if (mLoading != null)
//		// mLoading.setVisibility(View.GONE);
//		// }
//		// }, 1000);
//		// }
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//
//		only_show_visited = getShowOnlyVisited();
//		mAdapter = new DistrictWatchClientAdapter(getActivity(), only_show_visited);
//		mAdapter.setObjectsPerPage(500);
//        mAdapter.setAutoload(false);
//
//		super.onCreate(savedInstanceState);
//	}
//
//	@BindView(R.id.swipe_container) SwipeRefreshLayout mSwipeRefresher;
//	// @BindView(R.id.loading) ProgressBar mLoading;
//
////	@BindView(R.id.footer) RelativeLayout mFooter;
////	@BindView(R.id.footerShadow) View mFooterShadow;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//        Log.d(TAG, "onCreateView");
//
//		mRootView = inflater.inflate(
//				R.layout.fragment_list_districtwatchclients, container, false);
//
//		ButterKnife.bind(this, mRootView);
//
////		if (only_show_visited) {
////			mFooter.setVisibility(View.GONE);
////			mFooterShadow.setVisibility(View.GONE);
////		}
//
//		/** Setting the list adapter for the ListFragment */
//
//		setListAdapter(mAdapter);
//
//		mSwipeRefresher.setOnRefreshListener(this);
//		mSwipeRefresher.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
//
//
//		refreshParseData();
//
//		return mRootView;
//	}
//
//	@Override
//	public void onListItemClick(ListView l, View v, int clientPosition, long id) {
//		DistrictWatchClient districtWatchClient = mAdapter.getItem(clientPosition);
//		// go to details view
////        DistrictWatchClient.Recent.setSelected(districtWatchClient);
////
////		Intent intent = new Intent(getActivity(),
////				DistrictWatchDetailsActivity.class);
////		startActivity(intent);
//
//        controller.performAction(TaskController.ACTION.OPEN, districtWatchClient, false);
//
//		super.onListItemClick(l, v, clientPosition, id);
//	}
//
//	@Override
//	public void onRefresh() {
//		refreshParseData();
//	}
//
//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		ButterKnife.unbind(this);
//	}
//
//
//	@Override
//	public void onEventMainThread(UpdateUIEvent ev) {
//		Object obj = ev.getObject();
//		if (!(obj instanceof Location || obj instanceof DistrictWatchClient)) {
//			return;
//		}
//
//		if (mAdapter != null) {
//			mAdapter.loadObjects();
//		}
//	}
//
//
//	/*
//
//	@OnClick(R.id.district_checkbox_showall)
//	public void showHideArrived(Switch button) {
//		boolean show = button.isChecked();
//		mAdapter.setShowArrived(show);
//	}
//
//	@OnClick(R.id.district_reset)
//	public void restart(Button button) {
//		final DistrictWatch districtWatch = DistrictWatch.Recent.getSelected();
//
//		if (districtWatch == null)
//			return;
//
//        new MaterialDialog.Builder(getActivity())
//                .title(R.string.restart)
//                .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
//                .callback(new MaterialDialog.ButtonCallback() {
//                              @Override
//                              public void onPositive(MaterialDialog dialog) {
//                                  performReset(districtWatch);
//                              }
//                          }
//                ).show();
//
//	}
//
//	private void performReset(DistrictWatch districtWatch) {
//		new DistrictWatchClient.QueryBuilder(true).matching(districtWatch)
//				.build()
//				.findInBackground(new FindCallback<DistrictWatchClient>() {
//
//					@Override
//					public void done(final List<DistrictWatchClient> objects,
//							ParseException e) {
//						if (e == null) {
//
//                            final List<DistrictWatchClient> saveList = Lists.newArrayList();
//
//							for (DistrictWatchClient districtWatchClient : objects) {
//                                if (districtWatchClient.isArrived())
//                                    saveList.addUnique(districtWatchClient);
//
//								districtWatchClient.reset();
////                                districtWatchClient.getAutomationStrategy().clearAllAutomaticReports();
//							}
//
//                            ParseObject.pinAllInBackground(DistrictWatchClient.PIN, objects, new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
////                                    geofencingModule.rebuildGeofences(new DistrictWatchClient(), TAG);
//
//									EventBusController.postUIUpdate(objects);
//
//                                    ParseObject.saveAllInBackground(saveList);
//                                }
//                            });
//
//
//
//						} else {
//							Toast.makeText(getActivity(),
//									getString(R.string.title_internet_missing),
//									Toast.LENGTH_LONG).show();
//						}
//
//					}
//				});
//	}
//
//	*/
//
//	@Override
//	public void onDetach() {
//		activity = null;
//		super.onDetach();
//	}
//
//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		this.activity = (MainActivity) getActivity();
//	}
//
//}
