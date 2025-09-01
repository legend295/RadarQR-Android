package com.radarqr.dating.android.utility.chipslayoutmanager.layouter.placer;


import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager;

public class PlacerFactory {

    private ChipsLayoutManager lm;

    public PlacerFactory(ChipsLayoutManager lm) {
        this.lm = lm;
    }

    public IPlacerFactory createRealPlacerFactory() {
        return new RealPlacerFactory(lm);
    }

    public IPlacerFactory createDisappearingPlacerFactory() {
        return new DisappearingPlacerFactory(lm);
    }

}
