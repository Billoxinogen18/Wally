package com.wally.wally.tip;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.tango.ContentFitter;

/**
 * Created by Meravici on 8/8/2016. yea
 */
public class TipManager implements WallyEventListener, ContentFitter.OnContentFitListener, MapEventListener {
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
        mTipView.hide();
    }

    @Override
    public void onProfileInit() {
        showTip(TipService.Tag.MAP_PROFILE);
    }

    @Override
    public void onPersonInit() {
        showTip(TipService.Tag.MAP_PERSON);
    }

    @Override
    public void onPublicFeedInit() {
        showTip(TipService.Tag.MAP_PUBLIC_FEED);
    }

    @Override
    public void onWallyEvent(WallyEvent event) {
        switch (event.getId()) {
            case WallyEvent.LOCALIZATION_START_ON_CLOUD_ADF:
            case WallyEvent.LOCALIZATION_START:
                showTip(TipService.Tag.LOCALIZATION);
                break;
            case WallyEvent.LEARNING_START:
                showTip(TipService.Tag.LEARNING);
                break;
            case WallyEvent.LOCALIZATION_START_AFTER_LEARNING:
                showTip(TipService.Tag.LOCALIZATION_AFTER_LEARNING);
                break;
            case WallyEvent.ON_NEW_CONTENT_DIALOG_SHOW:
                showTip(TipService.Tag.NEW_CONTENT);
                break;
            case WallyEvent.LEARNING_FINISH:
            case WallyEvent.LOCALIZATION_FINISH_AFTER_LEARNING:
            case WallyEvent.LOCALIZATION_FINISH_AFTER_CLOUD_ADF:
            case WallyEvent.LOCALIZATION_FINISH_AFTER_SAVED_ADF:
            default:
                mTipView.hide();
                break;
        }
    }

    private void showTip(String type) {
        Tip tip = mTipService.getRandom(type);
        if (tip != null && !mTipService.isDisabled(tip.getId())) {
            mTipView.show(tip, 4000);
        }
    }

    ///////////////////////////////////// not needed methods ///////////////////////////////////////
    @Override
    public void onContentFit(TangoPoseData pose) {
        // Not needed
    }
}
