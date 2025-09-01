package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;

public class LTRForwardColumnBreaker implements ILayoutRowBreaker {

    @Override
    public boolean isRowBroke(AbstractLayouter al) {
        return al.getViewTop() > al.getCanvasTopBorder()
                && al.getViewTop() + al.getCurrentViewHeight() > al.getCanvasBottomBorder();
    }
}
