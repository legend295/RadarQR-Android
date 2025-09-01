package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.Item;

import java.util.List;

class EmptyRowStrategy implements IRowStrategy {
    @Override
    public void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row) {
        //do nothing
    }
}
