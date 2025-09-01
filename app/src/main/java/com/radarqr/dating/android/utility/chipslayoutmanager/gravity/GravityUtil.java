package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

abstract class GravityUtil {

    static int getHorizontalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasRightBorder() - layouter.getCanvasLeftBorder() - layouter.getRowLength();
    }

    static int getVerticalDifference(AbstractLayouter layouter) {
        return layouter.getCanvasBottomBorder() - layouter.getCanvasTopBorder() - layouter.getRowLength();
    }

}
