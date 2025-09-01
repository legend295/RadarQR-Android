package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

class CriteriaDownLayouterFinished implements IFinishingCriteria {

    private boolean isFinished;

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        isFinished = isFinished || abstractLayouter.getViewTop() >= abstractLayouter.getCanvasBottomBorder();
        return isFinished;
    }

}
