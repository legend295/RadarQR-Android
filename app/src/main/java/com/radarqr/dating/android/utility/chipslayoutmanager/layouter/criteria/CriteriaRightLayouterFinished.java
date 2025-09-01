package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaRightLayouterFinished implements IFinishingCriteria {

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return abstractLayouter.getViewLeft() >= abstractLayouter.getCanvasRightBorder();
    }
}
