package com.guardswift.ui.parse.data.client;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.guardswift.R;
import com.guardswift.ui.activity.AbstractToolbarActivity;

public class ClientListActivity extends AbstractToolbarActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, ClientListActivity.class));
    }

    @Override
    protected Fragment getFragment() {
        return ClientListFragment.newInstance();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.clients);
    }

    @Override
    protected String getToolbarSubTitle() {
        return null;
    }

}
