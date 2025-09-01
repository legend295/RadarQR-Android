package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

abstract class FinishingCriteriaDecorator implements IFinishingCriteria {

    private IFinishingCriteria finishingCriteria;

    FinishingCriteriaDecorator(IFinishingCriteria finishingCriteria) {
        this.finishingCriteria = finishingCriteria;
    }

    @Override
    public boolean isFinishedLayouting(AbstractLayouter abstractLayouter) {
        return finishingCriteria.isFinishedLayouting(abstractLayouter);
    }
}
