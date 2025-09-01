package com.radarqr.dating.android.utility.imageZoom;

import android.app.Activity;
import android.view.ViewGroup;


public class ActivityContainer implements TargetContainer {

    private Activity mActivity;

    ActivityContainer(Activity activity){
        this.mActivity = activity;
    }

    @Override
    public ViewGroup getDecorView(){
        return (ViewGroup) mActivity.getWindow().getDecorView();

    }
}
