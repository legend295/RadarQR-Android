package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import android.view.Gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.SpanLayoutChildGravity;

public class CenterChildGravity implements IChildGravityResolver {
    @Override
    @SpanLayoutChildGravity
    public int getItemGravity(int position) {
        return Gravity.CENTER_VERTICAL;
    }
}
