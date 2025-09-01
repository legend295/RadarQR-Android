package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import android.graphics.Rect;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.Item;

import java.util.List;

class RTLRowFillSpaceCenterDenseStrategy implements IRowStrategy {

    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        int difference = GravityUtil.getHorizontalDifference(abstractLayouter) / 2;

        for (Item item : row) {
            Rect childRect = item.getViewRect();
            childRect.left -= difference;
            childRect.right -= difference;
        }
    }
}
