package com.guardswift.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class GenericToolbarActivity extends AbstractToolbarActivity {

    protected static String title;
    protected static String subtitle;
    protected static Fragment fragment;

    public static void start(Context context, int title, Fragment fragment) {
        GenericToolbarActivity.start(context, context.getString(title), "", fragment);
    }

    public static void start(Context context, int title, int subtitle, Fragment fragment) {
        GenericToolbarActivity.start(context, context.getString(title), context.getString(subtitle), fragment);
    }

    public static void start(Context context, String title, String subtitle, Fragment fragment) {
        GenericToolbarActivity.title = title;
        GenericToolbarActivity.subtitle = subtitle;
        GenericToolbarActivity.fragment = fragment;
        context.startActivity(new Intent(context, GenericToolbarActivity.class));
    }

    @Override
    protected void onDestroy() {
        fragment = null;
        super.onDestroy();
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
