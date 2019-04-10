package com.dpdlad.radarview;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * @author Praveen Kumar on 14/06/2017
 *
 * This class used to apply animation on splitted arc drawing as progress bar
 */
public class RadarAnimation extends Animation {

    private RadarView starView;
    private int oldAngle;
    private int newAngle;

    private RadarAnimation(RadarView starView) {
        this.starView = starView;
        this.oldAngle = starView.getSweepAngle();
        this.newAngle = starView.getMaxAngle();
    }


    static void startArcProgressAnimation(RadarView starView) {
        RadarAnimation animation = new RadarAnimation(starView);
        animation.setDuration(3000);
        animation.setRepeatCount(AlphaAnimation.INFINITE);
        starView.startAnimation(animation);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        int angle = (int) (oldAngle + ((newAngle - oldAngle) * interpolatedTime));
        if (starView.getSweepAngle() < starView.getMaxAngle()) {
            starView.setSweepAngle(angle);
        } else {
            starView.setSweepAngle(0);
//            Log.e("$$$$$$$$$", "applyTransformation is Cancelled");
        }
        starView.invalidate();
    }
}