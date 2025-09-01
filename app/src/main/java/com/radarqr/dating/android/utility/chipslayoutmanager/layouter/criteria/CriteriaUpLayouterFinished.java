package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaUpLayouterFinished implements IFinishingCriteria {

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return abstractLayouter.getViewBottom() <= abstractLayouter.getCanvasTopBorder();
    }
}
