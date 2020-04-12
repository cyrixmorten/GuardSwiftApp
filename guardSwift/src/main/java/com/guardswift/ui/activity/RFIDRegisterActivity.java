package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.guardswift.util.RFIDUtils;


public class RFIDRegisterActivity extends AbstractToolbarActivity {

    private static final String TAG = RFIDRegisterActivity.class.getSimpleName();


    private String id = "Reading";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        id = RFIDUtils.readIdFromIntent(getIntent());
        setToolbarSubTitle(id);
    }

    @Override
    protected Fragment getFragment() {
        return null;
    }

    @Override
    protected String getToolbarTitle() {
        return null;
    }

    @Override
    protected String getToolbarSubTitle() {
        return id;
    }
}