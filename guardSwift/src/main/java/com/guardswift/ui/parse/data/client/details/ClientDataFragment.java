package com.guardswift.ui.parse.data.client.details;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import com.guardswift.ui.map.BaseMapFragment;
import com.parse.ParseGeoPoint;

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


    private Client.ObservableClient mClientObservable;
    private ClientViewModel mClientViewModel;
    private String objectId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            objectId = getArguments().getString(ARG_CLIENT_ID, "");
        }

        mClientViewModel = ViewModelProviders.of(this).get(ClientViewModel.class);
    }

    @Override
    protected int getMapLayoutId() {
        return R.id.layout_map;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final FragmentClientDetailsBinding binding = FragmentClientDetailsBinding.inflate(inflater);

        mClientViewModel.getClient(this.objectId)
                .observe(this, (client) -> {
                    mClientObservable = new Client.ObservableClient(client);
                    binding.setClient(mClientObservable);
                });

        binding.setHandler(this);

        return binding.getRoot();
    }



    @Override
    public void onMapReady(GoogleMap map) {

        Context context = getContext();

        if (context == null) {
            return;
        }

        Log.d(TAG, "onMapReady");

        mClientViewModel.getClient(this.objectId).observe(this, client -> {
            if (client == null) {
                return;
            }

            ParseGeoPoint position = client.getPosition();
            LatLng mapPosition = new LatLng(position.getLatitude(), position.getLongitude());

            map.addMarker(new MarkerOptions()
                    .position(mapPosition)
                    .title(client.getName())
                    .snippet(client.getFullAddress()));

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPosition, 15));


            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(true);
        });
    }

    public void save(View v) {
        Log.d(TAG, "SAVE");
        mClientViewModel.getClient(this.objectId).observe(this, client -> {
            if (client == null) {
                Log.d(TAG, "CLIENT NULL");
                return;
            }

            Log.d(TAG, "CLIENT UPDATE");
            client.updateFromObservable(mClientObservable);
            client.saveEventuallyAndNotify();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }






}
