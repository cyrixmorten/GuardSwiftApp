package com.guardswift.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @BindView(R.id.sliding_close_btn)
    ImageView mSlideClose;

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
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                super.onPanelStateChanged(panel, previousState, newState);

                mSlideClose.setVisibility((newState == mPanelDefaultState) ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                super.onPanelSlide(panel, slideOffset);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
    }

    @OnClick(R.id.sliding_close_btn)
    public void closeSlidingUpPanel() {
        mLayout.setPanelState(mPanelDefaultState);
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        setToolbarTitle(getToolbarTitle());
        setToolbarSubTitle(getToolbarSubTitle());
    }

    protected void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            return;
        }

        if (title != null && !title.isEmpty()) {
            actionBar.setTitle(title);
        }
    }


    protected void setToolbarSubTitle(String subTitle) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            return;
        }

        if (subTitle != null && !subTitle.isEmpty()) {
            actionBar.setSubtitle(subTitle);
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
