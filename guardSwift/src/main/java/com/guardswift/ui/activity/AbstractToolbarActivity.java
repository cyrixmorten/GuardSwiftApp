package com.guardswift.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.ui.helpers.ViewHelper;
import com.sothree.slidinguppanel.ScrollableViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guardswift.ui.activity.GenericToolbarActivity.fragment;

public abstract class AbstractToolbarActivity extends InjectingAppCompatActivity implements SlidingPanelActivity {

    private static final String TAG = AbstractToolbarActivity.class.getSimpleName();

    private final String FRAGMENT_TAG = "abstract:toolbar:fragment";

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mLayout;

    @BindView(R.id.sliding_layout_title)
    LinearLayout mSlideTitleLayout;

    @BindView(R.id.sliding_title)
    TextView mSlideTitle;

    @BindView(R.id.sliding_subtitle)
    TextView mSlideSubTitle;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.content)
    RelativeLayout content;

    private SlidingUpPanelLayout.PanelState mPanelDefaultState = SlidingUpPanelLayout.PanelState.HIDDEN;

    protected abstract Fragment getFragment();

    protected abstract String getToolbarTitle();

    protected abstract String getToolbarSubTitle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.gs_activity_toolbar);

        ButterKnife.bind(this);
        setupToolbar();

        FragmentManager fm = getSupportFragmentManager();

        Fragment contentFragment;
        if (savedInstanceState == null) {
            contentFragment = getFragment();
        } else {
            contentFragment = fm.findFragmentByTag(FRAGMENT_TAG);
        }


        if (contentFragment != null) {
            fm.beginTransaction().replace(R.id.content, contentFragment, FRAGMENT_TAG).commitNow();
        }

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
//        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
//
//            @Override
//            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//                super.onPanelStateChanged(panel, previousState, newState);
//
//                if (newState == mPanelDefaultState) {
//                    toolbar.getBackground().setAlpha(100);
//                }
//            }
//
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//                int offSetAlpha = Math.round(100 * slideOffset);
//
//                mSlideTitleLayout.getBackground().setAlpha(offSetAlpha);
//            }
//        });
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getToolbarTitle() != null && !getToolbarTitle().isEmpty()) {
            actionBar.setTitle(getToolbarTitle());
        }
        if (getToolbarSubTitle() != null && !getToolbarSubTitle().isEmpty()) {
            actionBar.setSubtitle(getToolbarSubTitle());
        }
    }

    public SlidingUpPanelLayout getSlidingPanelLayout() {
        return mLayout;
    }

    @Override
    public void setSlidingTitle(String title, String subtitle) {
        mSlideTitle.setText(title);
        mSlideSubTitle.setText(subtitle);
    }

    public void setSlidingContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.sliding_layout_body, fragment).commitNow();
    }

    public void setSlidingScrollView(View scrollView) {
        mLayout.setVerticalScrollBarEnabled(true);
        mLayout.setScrollableView(scrollView);
    }

    @Override
    public void setSlidingStateOnBackpressed(SlidingUpPanelLayout.PanelState state) {
        mPanelDefaultState = state;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(mPanelDefaultState);
        } else {
            super.onBackPressed();
        }
    }

}
