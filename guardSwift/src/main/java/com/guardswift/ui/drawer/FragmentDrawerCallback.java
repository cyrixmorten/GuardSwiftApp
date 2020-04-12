package com.guardswift.ui.drawer;

import androidx.fragment.app.Fragment;

/**
 * Created by cyrixmorten on 11/04/2017.
 */

public interface FragmentDrawerCallback {
    void setViewId(int viewId);

    void selectItem(long action);

    void selectItem(Fragment fragment, String title);

    void selectItem(Fragment fragment, String title, String subtitle);

    void selectItem(Fragment fragment, int titleResource);
}
