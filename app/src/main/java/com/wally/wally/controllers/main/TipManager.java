package com.wally.wally.controllers.main;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.EventListener;
import com.wally.wally.tip.Tip;
import com.wally.wally.tip.TipService;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public class TipManager implements EventListener, ContentFitter.OnContentFitListener {
    private TipView mTipView;
    private TipService mTipService;

    public TipManager(TipView tipView, TipService tipService){
        mTipView = tipView;
        mTipService = tipService;
    }

    @Override
    public void onLearningStart() {
        showTip(TipService.Tag.LEARNING);
    }

    @Override
    public void onLocalizationStart() {
        showTip(TipService.Tag.LOCALIZATION);
    }

    @Override
    public void onContentFit(TangoPoseData pose) {
        //Not needed
    }

    @Override
    public void onFitStatusChange(boolean fittingStarted) {
        if(fittingStarted){
            showTip(TipService.Tag.FITTING);
        }
    }

    @Override
    public void onContentFittingFinished(Content content) {

    }


    private void showTip(String type){
        Tip tip = mTipService.getRandom(type);
        mTipView.show(tip.getTitle(), tip.getMessage(), 5000);
    }
}
