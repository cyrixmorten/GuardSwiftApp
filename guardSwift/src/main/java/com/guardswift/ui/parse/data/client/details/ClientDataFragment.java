package com.guardswift.ui.parse.data.client.details;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guardswift.R;
import com.guardswift.databinding.FragmentClientDetailsBinding;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.query.ClientQueryBuilder;
import com.guardswift.ui.map.BaseMapFragment;
import com.guardswift.util.ToastHelper;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import de.greenrobot.event.EventBus;


public class ClientDataFragment extends BaseMapFragment {



    protected static final String TAG = ClientDataFragment.class
            .getSimpleName();

    public static ClientDataFragment newInstance(Client client) {

        ClientDataFragment fragment = new ClientDataFragment();


        Bundle args = new Bundle();
        args.putString(ARG_CLIENT_ID, client.getObjectId());
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_CLIENT_ID = "id";

    public ClientDataFragment() {
    }

    private ObservableClient mClient;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            String objectId = getArguments().getString(ARG_CLIENT_ID, "");

            ParseQuery<Client> query = new ClientQueryBuilder(false).matchingObjectId(objectId).build();
            try {
                Client client = query.getFirst();
                mClient = new ObservableClient(client);
            } catch (ParseException e) {
                ToastHelper.toast(getContext(), e.getMessage());
            }
        }


    }

    @Override
    protected int getMapLayoutId() {
        return R.id.layout_map;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentClientDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_client_details, container, false);

        View view = binding.getRoot();

        binding.setClient(mClient);

        return view;
    }



    private class MapData {
        private CircleOptions regularTaskRadius;
        private CircleOptions raidTaskRadius;

        public CircleOptions getRegularTaskRadius() {
            return regularTaskRadius;
        }

        public void setRegularTaskRadius(CircleOptions regularTaskRadius) {
            this.regularTaskRadius = regularTaskRadius;
        }

        public CircleOptions getRaidTaskRadius() {
            return raidTaskRadius;
        }

        public void setRaidTaskRadius(CircleOptions raidTaskRadius) {
            this.raidTaskRadius = raidTaskRadius;
        }
    }

    private MapData mapData;

    @Override
    public void onMapReady(GoogleMap map) {

        Context context = getContext();

        if (context == null) {
            return;
        }

        Log.d(TAG, "onMapReady");

        mapData = new MapData();



        ParseGeoPoint position = mClient.position.get();
        LatLng mapPosition = new LatLng(position.getLatitude(), position.getLongitude());

        map.addMarker(new MarkerOptions()
                .position(mapPosition)
                .title(mClient.name.get())
                .snippet(mClient.fullAddress.get()));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPosition, 15));

        // Zoom in, animating the camera.
//		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
//		map.setIndoorEnabled(true);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

//        map.getUiSettings().setAllGesturesEnabled(false);

//        mapData.setRegularTaskRadius(createCircle(mClient.getRadius(ParseTask.TASK_TYPE.REGULAR), R.color.md_green_500));
//        mapData.setRaidTaskRadius(createCircle(mClient.getRadius(ParseTask.TASK_TYPE.RAID), R.color.md_blue_500));
//
//        map.addCircle(mapData.getRegularTaskRadius());
//        map.addCircle(mapData.getRaidTaskRadius());
    }


//    private CircleOptions createCircle(int radius, int color) {
//        ParseGeoPoint position = mClient.position.get();
//
//        return new CircleOptions()
//                .center(new LatLng(position.getLatitude(), position.getLongitude()))
//                .radius(radius).strokeColor(Color.BLACK)
//                .strokeWidth(2).fillColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), color), 50));
//
//
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

//    public void startGoogleMapsNavigation() {
//        LatLng clientPosition = mClientPositionDetails.getMapPosition();
//        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%f,%f (%s)", clientPosition.latitude, clientPosition.longitude, mClientPositionDetails.getMapTitle());
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//        try {
//            startActivity(intent);
//        } catch (ActivityNotFoundException ex) {
//            try {
//                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                startActivity(unrestrictedIntent);
//            } catch (ActivityNotFoundException innerEx) {
//                Toast.makeText(getContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    public static class ObservableClient {

//        ObservableArrayMap<String, Object> client = new ObservableArrayMap<>();

        public final ObservableField<String> id = new ObservableField<>();
        public final ObservableField<String> name = new ObservableField<>();
        public final ObservableField<String> fullAddress = new ObservableField<>();
        public final ObservableField<ParseGeoPoint> position = new ObservableField<>();

        ObservableClient(Client client) {
            id.set(client.getId());
            name.set(client.getName());
            fullAddress.set(client.getFullAddress());
            position.set(client.getPosition());
        }

    }


}
