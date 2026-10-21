package com.example.android.mymap.models;

/*
import android.view.View;
import android.widget.LinearLayout;

public class ViewWeightAnimationWrapper {
    private View view;

    public ViewWeightAnimationWrapper(View view) {
        if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            this.view = view;
        } else {
            throw new IllegalArgumentException("The view should have LinearLayout as parent");
        }
    }

    public void setWeight(float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.getParent().requestLayout();
    }

    public float getWeight() {
        return ((LinearLayout.LayoutParams) view.getLayoutParams()).weight;
    }
}*/

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ViewWeightAnimationWrapper extends Animation {

    private float mStartWeight;
    private final float mEndWeight;
    private final LinearLayout mContent;

    public ViewWeightAnimationWrapper(LinearLayout content, float startWeight, float endWeight) {
        mStartWeight = startWeight;
        mEndWeight = endWeight;
        mContent = content;
    }

    @Override
    protected void applyTransformation(float interpolatedTime,
                                       Transformation t) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContent
                .getLayoutParams();
        mStartWeight = params.weight;
        params.weight = (mStartWeight + ((mEndWeight - mStartWeight) * interpolatedTime));
        mContent.setLayoutParams(params);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
