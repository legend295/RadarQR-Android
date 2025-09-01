package com.radarqr.dating.android.utility.chipslayoutmanager.anchor;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.ICanvas;


abstract class AbstractAnchorFactory implements IAnchorFactory {
    RecyclerView.LayoutManager lm;
    private ICanvas canvas;

    AbstractAnchorFactory(RecyclerView.LayoutManager lm, ICanvas canvas) {
        this.lm = lm;
        this.canvas = canvas;
    }

    ICanvas getCanvas() {
        return canvas;
    }

    AnchorViewState createAnchorState(View view) {
        return new AnchorViewState(lm.getPosition(view), canvas.getViewRect(view));
    }

    @Override
    public AnchorViewState createNotFound() {
        return AnchorViewState.getNotFoundState();
    }

}
