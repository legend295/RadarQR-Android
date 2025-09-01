package com.radarqr.dating.android.utility.chipslayoutmanager.gravity;


import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.AbstractLayouter;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.Item;

import java.util.List;

public interface IRowStrategy {
    void applyStrategy(AbstractLayouter abstractLayouter, List<Item> row);
}
