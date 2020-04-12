package com.guardswift.ui.activity;

import androidx.fragment.app.Fragment;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public interface SlidingPanelActivity {
    SlidingUpPanelLayout getSlidingPanelLayout();
    void setSlidingTitle(String title, String subtitle);
    void setSlidingContent(Fragment fragment);
    void setSlidingScrollView(View scrollView);
    void setSlidingStateOnBackpressed(SlidingUpPanelLayout.PanelState state);
}
