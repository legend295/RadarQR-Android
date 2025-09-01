package com.radarqr.dating.android.utility.chipslayoutmanager.util;

import android.view.View;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.IStateFactory;

public class StateHelper {
    public static boolean isInfinite(IStateFactory stateFactory) {
        return stateFactory.getSizeMode() == View.MeasureSpec.UNSPECIFIED
                && stateFactory.getEnd() == 0;
    }
}
