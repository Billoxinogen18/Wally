package com.wally.wally.controllers.main;

import com.wally.wally.tango.EventListener;
import com.wally.wally.tip.Tip;
import com.wally.wally.tip.TipService;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public class TipManager implements EventListener {
    private TipView mTipView;
    private TipService mTipService;

    public TipManager(TipView tipView, TipService tipService){
        mTipView = tipView;
        mTipService = tipService;
    }

    @Override
    public void onLearningStart() {
        Tip tip = mTipService.getRandom(TipService.Tag.LEARNING);
        mTipView.show(tip.getTitle(), tip.getMessage(), 5000);
    }
}
