package com.guardswift.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class CheckableFrameLayout extends FrameLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_activated,
            android.R.attr.state_checked,
    };

    private boolean mChecked;
    private List<Checkable> checkableViews;

    public CheckableFrameLayout(Context context) {
        super(context);
    }

    public CheckableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

        if (mChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.checkableViews = new ArrayList<>();
        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            findCheckableChildren(this.getChildAt(i));
        }
    }

    /**
     * Add to our checkable list all the children of the view that implement the
     * interface Checkable
     */
    private void findCheckableChildren(View v) {
        if (v instanceof Checkable) {
            this.checkableViews.add((Checkable) v);
        }
        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }


    // @see android.widget.Checkable#isChecked()
    public boolean isChecked() {
        return mChecked;
    }

    // @see android.widget.Checkable#setChecked(boolean)
    public void setChecked(boolean isChecked) {
        if (mChecked != isChecked) {
            mChecked = isChecked;
            invalidate();
            refreshDrawableState();

            for (Checkable c : checkableViews) {
                // Pass the information to all the child Checkable widgets
                c.setChecked(isChecked);
            }
        }
    }

    // @see android.widget.Checkable#toggle()
    public void toggle() {
        this.mChecked = !this.mChecked;
        for (Checkable c : checkableViews) {
            // Pass the information to all the child Checkable widgets
            c.toggle();
        }
    }
}