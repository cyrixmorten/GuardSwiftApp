package com.guardswift.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class GenericToolbarActivity extends AbstractToolbarActivity {

    private static String title;
    private static String subtitle;
    private static Fragment fragment;

    public static void start(Context context, String title, String subtitle, Fragment fragment) {
        GenericToolbarActivity.title = title;
        GenericToolbarActivity.subtitle = subtitle;
        GenericToolbarActivity.fragment = fragment;
        context.startActivity(new Intent(context, GenericToolbarActivity.class));
    }

    @Override
    protected Fragment getFragment() {
        return fragment;
    }

    @Override
    protected String getToolbarTitle() {
        return title;
    }

    @Override
    protected String getToolbarSubTitle() {
        return subtitle;
    }

}