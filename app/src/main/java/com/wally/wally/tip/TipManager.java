package com.wally.wally.tip;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.tango.ContentFitter;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public class TipManager implements WallyEventListener, ContentFitter.OnContentFitListener {
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

//    @Override
    public void onLearningStart() {
        showTip(TipService.Tag.LEARNING);
    }

//    @Override
    public void onLocalizationStart() {
        showTip(TipService.Tag.LOCALIZATION);
    }

//    @Override
    public void onTangoOutOfDate() {

    }

    ///////////////////////////////////// not needed methods ///////////////////////////////////////
    @Override
    public void onContentFit(TangoPoseData pose) {
        // Not needed
    }

    @Override
    public void onWallyEvent(WallyEvent event) {
        switch (event.getId()) {
            case WallyEvent.LEARNING_START:
                onLearningStart();
                break;
            case WallyEvent.TANGO_OUT_OF_DATE:
                onTangoOutOfDate();
                break;
            case WallyEvent.LOCALIZATION_START:
                onLocalizationStart();
                break;
            default:
                break;
        }
    }

}
