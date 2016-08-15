package com.wally.wally.controllers.main;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.events.EventListener;
import com.wally.wally.tip.Tip;
import com.wally.wally.tip.TipService;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public class TipManager implements EventListener, ContentFitter.OnContentFitListener {
    private TipView mTipView;
    private TipService mTipService;

    public TipManager(TipView tipView, TipService tipService) {
        mTipView = tipView;
        mTipService = tipService;

        mTipView.setDismissListener(new TipView.DismissListener() {
            @Override
            public void onDismiss(String id) {
                mTipService.disableTip(id);
            }
        });
    }

    @Override
    public void onTangoReady() {

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
    public void onFitStatusChange(boolean fittingStarted) {
        if (fittingStarted) {
            showTip(TipService.Tag.FITTING);
        }
    }

    @Override
    public void onContentFittingFinished(Content content) {

    }

    private void showTip(String type) {
        Tip tip = mTipService.getRandom(type);
        mTipView.show(tip.getMessage(), "", 0);
    }

    @Override
    public void onTangoOutOfDate() {

    }

    @Override
    public void onLearningFinish() {

    }

    @Override
    public void onLocalizationStartAfterLearning() {

    }

    @Override
    public void onLocalizationFinishAfterLearning() {

    }

    @Override
    public void onLocalizationFinishAfterSavedAdf() {

    }

    ///////////////////////////////////// not needed methods ///////////////////////////////////////
    @Override
    public void onContentFit(TangoPoseData pose) {
        //Not needed
    }
}
