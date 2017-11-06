package com.guardswift.ui.parse.data.client.details;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;

import java.util.Map;


public class ClientDetailsViewpagerFragment extends AbstractTabsViewPagerFragment {


    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    public static ClientDetailsViewpagerFragment newInstance(Client client) {

        ClientDetailsViewpagerFragment fragment = new ClientDetailsViewpagerFragment();

        fragment.mClient = client;

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private Client mClient;

    public ClientDetailsViewpagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.data), ClientDataFragment.newInstance(mClient));
        fragmentMap.put(getString(R.string.client_contacts), ClientDataFragment.newInstance(mClient));

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }

}
