package com.samilcts.app.mpaio.demo2.util;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mskim on 2016-01-12.
 * mskim@31cts.com
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class BadgeBehavior extends CoordinatorLayout.Behavior<View> {

    public BadgeBehavior(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

     //   Log.w("BadgeBehavior", "onDependentViewChanged");

     //   Log.w("BadgeBehavior", "dependency.getHeight" + dependency.getHeight() );


        if (parent.doViewsOverlap(dependency, child)) {
             float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());

            child.setTranslationY(translationY);
        }

        return false;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {

        return dependency instanceof Snackbar.SnackbarLayout;
    }
}