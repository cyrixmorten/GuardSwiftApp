package com.guardswift.ui.helpers;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.view.View;

import java.util.List;

/**
 * Created by cyrixmorten on 06/06/2017.
 */

public class ViewHelper {

    public static class TintBackground {


        public static <V extends View> void tintBackgroundColor(List<V> views,  int colorTo, final int alpha, int duration) {
            for (View view : views) {
                tintBackgroundColor(view, ((ColorDrawable)view.getBackground()).getColor(), colorTo, alpha, duration);
            }
        }

        public static <V extends View> void tintBackgroundColor(List<V> views, int colorFrom, int colorTo, final int alpha, int duration) {
            for (View view : views) {
                tintBackgroundColor(view, colorFrom, colorTo, alpha, duration);
            }
        }

        public static <V extends View> void tintBackgroundColor(final V view, int color, int alpha) {
            int toColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
            if (view instanceof CardView) {
                ((CardView) view).setCardBackgroundColor(toColor);
            } else {
                view.setBackgroundColor(toColor);
            }
        }

        public static <V extends View> void tintBackgroundColor(V view,  int colorTo, final int alpha, int duration) {
            tintBackgroundColor(view, ((ColorDrawable)view.getBackground()).getColor(), colorTo, alpha, duration);
        }

        public static <V extends View> void tintBackgroundColor(final V view, int colorFrom, int colorTo, final int alpha, int duration) {
            if (duration == 0) {
                tintBackgroundColor(view, colorTo, alpha);
                return;
            }

            final float[] from = new float[3],
                    to = new float[3];


            Color.colorToHSV(colorFrom, from);   // from white
            Color.colorToHSV(colorTo, to);     // to red

            ValueAnimator anim = ValueAnimator.ofFloat(0, 1);   // animate from 0 to 1
            anim.setDuration(duration);                              // for 300 ms

            final float[] hsv = new float[3];                  // transition color
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // Transition along each axis of HSV (hue, saturation, value)
                    hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                    hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                    hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                    int color = Color.HSVToColor(alpha, hsv);
                    if (view instanceof CardView) {
                        ((CardView) view).setCardBackgroundColor(color);
                    } else {
                        view.setBackgroundColor(color);
                    }
                }
            });

            anim.start();
        }
    }
}
