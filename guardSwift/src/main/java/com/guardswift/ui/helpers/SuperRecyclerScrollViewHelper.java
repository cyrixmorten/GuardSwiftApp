package com.guardswift.ui.helpers;

import android.util.Log;
import android.view.View;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.sothree.slidinguppanel.ScrollableViewHelper;


// https://github.com/umano/AndroidSlidingUpPanel
public class SuperRecyclerScrollViewHelper extends ScrollableViewHelper {
    public int getScrollableViewScrollPosition(View scrollableView, boolean isSlidingUp) {
        boolean isSuperRecycle = scrollableView instanceof SuperRecyclerView;
        Log.d("ViewHelper", "isSuperRecycle: " + isSuperRecycle);

        if (scrollableView instanceof SuperRecyclerView) {
            Log.d("ViewHelper", "isSlidingUp" + isSlidingUp);
            return scrollableView.getScrollY();
//            if(isSlidingUp){
//                return scrollableView.getScrollY();
//            } else {
//                SuperRecyclerView srv = ((SuperRecyclerView) scrollableView);
//                View child = srv.getChildAt(0);
//                return (child.getBottom() - (srv.getHeight() + srv.getScrollY()));
//            }
        } else {
            return 0;
        }
    }
}