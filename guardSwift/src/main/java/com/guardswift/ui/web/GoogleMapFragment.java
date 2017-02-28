package com.guardswift.ui.web;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class GoogleMapFragment extends SupportMapFragment {

	private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";
	private static final String TAG = GoogleMapFragment.class.getSimpleName();

	private OnMapReadyCallback mCallback;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public static GoogleMapFragment newInstance() {
		return new GoogleMapFragment();
	}

	public static GoogleMapFragment newInstance(GoogleMapOptions options) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

		GoogleMapFragment fragment = new GoogleMapFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnMapReadyCallback) activity;
		} catch (ClassCastException e) {
			Fragment fragment = getParentFragment();
			if (fragment != null) {
				try {
					mCallback = (OnMapReadyCallback) fragment;
				} catch (ClassCastException e1) {
					throw new ClassCastException(fragment.getClass().getName()
							+ " must implement OnGoogleMapFragmentListener");
				}

			} else {
				throw new ClassCastException(getActivity().getClass().getName()
						+ " must implement OnGoogleMapFragmentListener");
			}
		}

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if (mCallback != null) {
			getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap googleMap) {
					mCallback.onMapReady(googleMap);
				}
			});

		}

		return view;
	}

	@Override
	public void onDetach() {
		mCallback = null;
		super.onDetach();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}
