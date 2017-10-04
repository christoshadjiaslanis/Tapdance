package com.example.admin.btnz.GameComponents.Btns;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Admin on 1/3/2016.
 */
class Ring {


    private ImageView ringImage;
    private int radiusInPixels;
    private final float RING_TO_BUTTON_RATIO = 1.7F;


    Ring(int image, int radiusInPixels, Context context, int x, int y, float z) {

        ringImage = new ImageView(context);
        this.radiusInPixels = radiusInPixels;

        ringImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ringImage.setBackgroundResource(image);
        ringImage.setX(x - (int) ((radiusInPixels / RING_TO_BUTTON_RATIO) * 1.01));
        ringImage.setY(y - (int) ((radiusInPixels / RING_TO_BUTTON_RATIO) * 1.01));
        ringImage.setZ(z - 0.00005F);

        ringImage.setLayoutParams(new LinearLayout.LayoutParams(Math.round(radiusInPixels * RING_TO_BUTTON_RATIO), Math.round(radiusInPixels * RING_TO_BUTTON_RATIO)));


    }

    void reduceSize() {


        ringImage.setLayoutParams(new RelativeLayout.LayoutParams(Math.round(radiusInPixels * RING_TO_BUTTON_RATIO), Math.round(radiusInPixels * RING_TO_BUTTON_RATIO)));
        ObjectAnimator anim = ObjectAnimator.ofFloat(ringImage, "alpha",0f, 1f);

        int RING_TIME = 1000;
        anim.setDuration(RING_TIME);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(ringImage, "scaleX", 1 / (RING_TO_BUTTON_RATIO * 2));
        anim2.setDuration(RING_TIME);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(ringImage, "scaleY",  1 / (RING_TO_BUTTON_RATIO * 2));
        anim3.setDuration(RING_TIME);

        anim.start();
        anim2.start();
        anim3.start();
    }

    ImageView getRingImage() {
        return ringImage;
    }
}
