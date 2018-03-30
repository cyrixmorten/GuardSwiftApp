package com.guardswift.ui.parse.data.client.details;

import android.content.Context;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

        FragmentClientDetailsBinding binding = FragmentClientDetailsBinding.inflate(inflater);

        binding.setClient(mClient);

        return binding.getRoot();
    }



    @Override
    public void onMapReady(GoogleMap map) {

        Context context = getContext();

        if (context == null) {
            return;
        }

        Log.d(TAG, "onMapReady");


        ParseGeoPoint position = mClient.position.get();
        LatLng mapPosition = new LatLng(position.getLatitude(), position.getLongitude());

        map.addMarker(new MarkerOptions()
                .position(mapPosition)
                .title(mClient.name.get())
                .snippet(mClient.fullAddress.get()));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPosition, 15));


        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    public static class ObservableClient {

//        ObservableArrayMap<String, Object> client = new ObservableArrayMap<>();

        public final ObservableField<String> id = new ObservableField<>();
        public final ObservableField<String> name = new ObservableField<>();
        public final ObservableField<String> fullAddress = new ObservableField<>();
        public final ObservableField<ParseGeoPoint> position = new ObservableField<>();

        private Client client;

        ObservableClient(Client client) {
            this.client = client;

            id.set(client.getId());
            name.set(client.getName());
            fullAddress.set(client.getFullAddress());
            position.set(client.getPosition());
        }

    }


}
