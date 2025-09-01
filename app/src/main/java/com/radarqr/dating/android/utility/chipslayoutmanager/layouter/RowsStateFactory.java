package com.radarqr.dating.android.utility.chipslayoutmanager.layouter;

import android.view.View;

import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager;
import com.radarqr.dating.android.utility.chipslayoutmanager.IScrollingController;
import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.AnchorViewState;
import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.IAnchorFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.anchor.RowsAnchorFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.gravity.RowGravityModifiersFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.breaker.DecoratorBreakerFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.AbstractCriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.ICriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.InfiniteCriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.criteria.RowsCriteriaFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.layouter.placer.IPlacerFactory;
import com.radarqr.dating.android.utility.chipslayoutmanager.util.StateHelper;

public class RowsStateFactory implements IStateFactory {

    private ChipsLayoutManager lm;

    public RowsStateFactory(ChipsLayoutManager lm) {
        this.lm = lm;
    }

    private IOrientationStateFactory createOrientationStateFactory() {
        return lm.isLayoutRTL() ? new RTLRowsOrientationStateFactory() : new LTRRowsOrientationStateFactory();
    }

    @Override
    public LayouterFactory createLayouterFactory(ICriteriaFactory criteriaFactory, IPlacerFactory placerFactory) {
        IOrientationStateFactory orientationStateFactory = createOrientationStateFactory();

        return new LayouterFactory(lm,
                orientationStateFactory.createLayouterCreator(lm),
                new DecoratorBreakerFactory(
                        lm.getViewPositionsStorage(),
                        lm.getRowBreaker(),
                        lm.getMaxViewsInRow(),
                        orientationStateFactory.createDefaultBreaker()),
                criteriaFactory,
                placerFactory,
                new RowGravityModifiersFactory(),
                orientationStateFactory.createRowStrategyFactory().createRowStrategy(lm.getRowStrategyType()));
    }

    @Override
    public AbstractCriteriaFactory createDefaultFinishingCriteriaFactory() {
        return StateHelper.isInfinite(this)? new InfiniteCriteriaFactory() : new RowsCriteriaFactory();
    }

    @Override
    public IAnchorFactory anchorFactory() {
        return new RowsAnchorFactory(lm, lm.getCanvas());
    }

    @Override
    public IScrollingController scrollingController() {
        return lm.verticalScrollingController();
    }

    @Override
    public ICanvas createCanvas() {
        return new RowSquare(lm);
    }

    @Override
    public int getSizeMode() {
        return lm.getHeightMode();
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public int getStart(View view) {
        return lm.getDecoratedTop(view);
    }

    @Override
    public int getStart(AnchorViewState anchor) {
        return anchor.getAnchorViewRect().top;
    }

    @Override
    public int getEnd() {
        return lm.getHeight();
    }

    @Override
    public int getEnd(View view) {
        return lm.getDecoratedBottom(view);
    }

    @Override
    public int getEnd(AnchorViewState anchor) {
        return anchor.getAnchorViewRect().bottom;
    }

    @Override
    public int getEndViewPosition() {
        return lm.getPosition(lm.getCanvas().getRightView());
    }

    @Override
    public int getStartAfterPadding() {
        return lm.getPaddingTop();
    }

    @Override
    public int getStartViewPosition() {
        return lm.getPosition(lm.getCanvas().getLeftView());
    }

    @Override
    public int getEndAfterPadding() {
        return lm.getHeight() - lm.getPaddingBottom();
    }

    @Override
    public int getStartViewBound() {
        return getStart(lm.getCanvas().getTopView());
    }

    @Override
    public int getEndViewBound() {
        return getEnd(lm.getCanvas().getBottomView());
    }

    @Override
    public int getTotalSpace() {
        return lm.getHeight() - lm.getPaddingTop()
                - lm.getPaddingBottom();
    }
}
