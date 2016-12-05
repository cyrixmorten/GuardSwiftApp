package com.guardswift.ui.parse.data.client;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientContact;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.web.GoogleMapFragment;
import com.guardswift.ui.web.GoogleMapFragment.OnGoogleMapFragmentListener;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ClientDetailsFragment extends InjectingFragment implements
		OnGoogleMapFragmentListener {

    public interface ClientPositionDetails {

        @NonNull LatLng getMapPosition();

        @NonNull String getMapTitle();

        @NonNull String getMapSnippet();

//        int getGeofenceRadius();
    }

	protected static final String TAG = ClientDetailsFragment.class
			.getSimpleName();

	public static ClientDetailsFragment newInstance(Context context, Client client) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);

		ClientDetailsFragment fragment = new ClientDetailsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public ClientDetailsFragment() {
	}

    @Inject
    ClientCache clientCache;
    @Inject
    GuardCache guardCache;

	private Client mClient;
    private Guard mGuard;
	private ClientPositionDetails mClientPositionDetails;
	private List<View> mDetailsViews;


	@Bind(R.id.mapContainer) RelativeLayout mapContainer;
//    @Bind(R.id.button_navigation)
//    Button startNavigationButton;
	@Bind(R.id.detailsContainer) LinearLayout detailsContainer;
//    @Bind(R.id.button_messages)
//    BootstrapButton button_messages;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mClient = clientCache.getSelected();
        mGuard = guardCache.getLoggedIn();
        mClientPositionDetails = new ClientPositionDetails() {
            @NonNull
            @Override
            public LatLng getMapPosition() {
                if (mClient != null) {
                    ParseGeoPoint position = mClient.getPosition();
                    return new LatLng(position.getLatitude(), position.getLongitude());
                }
                return new LatLng(0,0);
            }

            @NonNull
            @Override
            public String getMapTitle() {
                if (mClient != null)
                    return mClient.getName();
                return "";
            }

            @NonNull
            @Override
            public String getMapSnippet() {
                if (mClient != null)
                    return mClient.getFullAddress();
                return "";
            }

        };
        mDetailsViews = getDetailsViews();

        EventBus.getDefault().register(this);
    }



    private List<View> getDetailsViews() {
        List<View> views = new ArrayList<View>();
        LayoutInflater li = LayoutInflater.from(getActivity());
        for (ParseObject contactObject: mClient.getContactsWithNames()) {
            if (!contactObject.isDataAvailable()) {
                continue;
            }

            ClientContact contact = (ClientContact)contactObject;
            View v = li.inflate(R.layout.view_client_contact, null);
            TextView name = (TextView) v.findViewById(R.id.name);
            TextView desc = (TextView) v.findViewById(R.id.desc);
            BootstrapButton phoneNumber = (BootstrapButton) v.findViewById(R.id.button_phoneNumber);
            name.setText(contact.getName());
            desc.setText(contact.getDesc());

            String phoneNumberString = contact.getPhoneNumber();
            phoneNumber.setText(phoneNumberString);
            phoneNumber.setTag(phoneNumberString);
            phoneNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumberString = (String) v.getTag();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumberString));
                    startActivity(intent);
                }
            });
            views.add(v);
        }
        return views;
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_client_details,
                container, false);

		ButterKnife.bind(this, rootView);

//        if (!(getActivity() instanceof AbstractTaskDetailsActivity)) {
//            startNavigationButton.setVisibility(View.GONE);
//        }

        addMapFragment();
        addDetailsViews();
//        updateMessagesButtonUI();

		return rootView;
	}

    private void addMapFragment() {

        final FragmentManager fm = getChildFragmentManager();

        GoogleMapFragment mapFragment = (GoogleMapFragment)fm.findFragmentByTag("map");

        if (mapFragment != null) {
            Log.e(TAG, "reusing map");
            fm.beginTransaction().attach(mapFragment).commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    onMapReady(googleMap);
                }
            });

        } else {
            Log.e(TAG, "new map");
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (isAdded()) {

                        GoogleMapFragment mapFragment = GoogleMapFragment
                                .newInstance();
                        fm.beginTransaction()
                                .replace(R.id.mapContainer, mapFragment, "map")
                                .commitAllowingStateLoss();
                    }
                }
            }, 500);
        }
    }


//    private void updateMessagesButtonUI() {
//        if (mClient.hasUnreadMessagesFor(mGuard)) {
//            button_messages.setType("warning");
//        } else {
//            button_messages.setType("success");
//        }
//    }

    public void onEventMainThread(UpdateUIEvent ev) {
        // do nothing
    }

//    @OnClick(R.id.button_navigation)
//    public void startNavigation() {
//        startGoogleMapsNavigation();
//    }


//    @OnClick(R.id.button_messages)
//    public void showMessages(BootstrapButton button) {
//
//        MessagesHolder.Recent.setSelected(mClient);
//        startActivity(new Intent(getActivity(), MessagesActivity.class));

//        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.slide_in_top,R.anim.slide_out_top,R.anim.slide_in_top,R.anim.slide_out_top);
//
//        android.support.v4.app.Fragment fragment = getChildFragmentManager().findFragmentByTag("client_messages");
//        if (fragment != null && fragment.isVisible()) {
//            // already onActionOpen, close it
//            ft.remove(fragment);
//            button.setRightIcon("fa-chevron-down");
//        } else {
//            ft.addUnique(R.id.container, MessagesFragment.newInstance(mClient), "client_messages");
//            button.setRightIcon("fa-chevron-up");
//        }
//        ft.commit();
//    }


	private long addDetailsViews() {
		if (!mDetailsViews.isEmpty())
			detailsContainer.removeAllViews();

		// long delay = 500;
		for (View v : mDetailsViews) {
			addDetailsView(v, 0);
			// delay += 100;
		}

		return 0;
	}

	private void addDetailsView(final View detailView, long delay) {
		new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (detailsContainer != null)
                    detailsContainer.addView(detailView);
            }
        }, delay);
	}

	@Override
	public void onMapReady(GoogleMap map) {

        if (mClientPositionDetails != null && mClientPositionDetails
                .getMapPosition() != null) {
            map.addMarker(new MarkerOptions().position(mClientPositionDetails
                    .getMapPosition()).title(mClientPositionDetails.getMapTitle()).snippet(mClientPositionDetails.getMapSnippet()))
            // .title(getMapTitle()).snippet(getMapSnippet()))
            // .showInfoWindow();
            ;

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    mClientPositionDetails.getMapPosition(), 15));
        }
        else {
            if (mClient != null) {
                Crashlytics.log(ParseUser.getCurrentUser().getUsername() + " missing mClientPositionDetails for client " + mClient.getName());
            }
        }

		// Zoom in, animating the camera.
//		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
//		map.setIndoorEnabled(true);
		map.setMyLocationEnabled(true);

//        displayGeofence(map);

	}

//    private void displayGeofence(GoogleMap map) {
//        LatLng pos = mClientPositionDetails.getMapPosition();
//            CircleOptions circleOptions1 = new CircleOptions()
//                    .center(new LatLng(pos.latitude, pos.longitude))
//                    .radius(mClientPositionDetails.getGeofenceRadius()).strokeColor(Color.BLACK)
//                    .strokeWidth(2).fillColor(0x500000ff);
//            map.addCircle(circleOptions1);
//    }

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	@Override
	public void onDetach() {
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}

    public void startGoogleMapsNavigation() {
        LatLng clientPosition = mClientPositionDetails.getMapPosition();
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%f,%f (%s)", clientPosition.latitude, clientPosition.longitude, mClientPositionDetails.getMapTitle());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        try
        {
            startActivity(intent);
        }
        catch(ActivityNotFoundException ex)
        {
            try
            {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(unrestrictedIntent);
            }
            catch(ActivityNotFoundException innerEx)
            {
                Toast.makeText(getContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        }
    }



}
