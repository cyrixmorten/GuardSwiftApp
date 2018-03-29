package com.guardswift.ui.drawer;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by cyrixmorten on 12/04/2017.
 */


public class ToolbarFragmentDrawerCallback implements FragmentDrawerCallback {

    public interface SelectActionCallback {
        void selectAction(long action);
    }

    private FragmentActivity activity;
    private Toolbar toolbar;
    private SelectActionCallback actionCallback;
    private int viewId = 0;

    public ToolbarFragmentDrawerCallback(FragmentActivity activity, Toolbar toolbar, int viewId) {
        this.activity = activity;
        this.toolbar = toolbar;

        setViewId(viewId);
    }



    public void setActionCallback(SelectActionCallback actionCallback) {
        this.actionCallback = actionCallback;
    }

    public void setActionBarTitle(final String title, final String subtitle) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(title);
                toolbar.setSubtitle(subtitle);
            }
        });
    }

    // delay a bit to allow navigation drawer to close before loading
    private void replaceFragment(final Fragment fragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!activity.isFinishing()) {
                    // commitAllowingStateLoss is a bit brutal
                    // but as state loss errors are happening very rarely plus we are not
                    // storing state on any of the fragments it is assumed
                    // to be ok to do here.
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(ToolbarFragmentDrawerCallback.this.viewId, fragment)
                            .commitNowAllowingStateLoss();
                }
            }
        }, 500);
    }

    @Override
    public void setViewId(int viewId) {
        this.viewId = viewId;
    }


    @Override
    public void selectItem(Fragment fragment, String title) {
        setActionBarTitle(title, "");
        replaceFragment(fragment);
    }

    @Override
    public void selectItem(Fragment fragment, String title, String subtitle) {
        setActionBarTitle(title, subtitle);
        replaceFragment(fragment);
    }

    @Override
    public void selectItem(Fragment fragment, int titleResource) {
        setActionBarTitle(activity.getString(titleResource), "");
        replaceFragment(fragment);
    }

    @Override
    public void selectItem(long action) {
        if (this.actionCallback != null) {
            this.actionCallback.selectAction(action);
        }
    }


}
