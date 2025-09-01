package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria;

import android.util.Log;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaLeftLayouterFinished implements IFinishingCriteria {
    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return abstractLayouter.getViewRight() <= abstractLayouter.getCanvasLeftBorder();
    }
}
