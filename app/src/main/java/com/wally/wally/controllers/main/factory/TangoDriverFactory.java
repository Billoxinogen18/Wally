package com.wally.wally.controllers.main.factory;

import android.content.Context;
import android.hardware.display.DisplayManager;

import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.adf.AdfService;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.objects.content.Content;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.progressUtils.LearningEvaluator;
import com.wally.wally.tango.TangoDriver;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.states.TangoForCloudAdfs;
import com.wally.wally.tango.states.TangoForLearnedAdf;
import com.wally.wally.tango.states.TangoForLearning;
import com.wally.wally.tango.states.TangoForReadyState;
import com.wally.wally.tango.states.TangoForSavedAdf;
import com.wally.wally.tango.states.TangoState;
import com.wally.wally.tango.states.TangoStateConnector;
import com.wally.wally.tip.TipManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shota on 8/15/16.
 * Factory for Tango States and tango driver
 */
public class TangoDriverFactory {

    private Config mConfig;
    private TangoUpdater mTangoUpdater;
    private TangoFactory mTangoFactory;
    private WallyRenderer mRenderer;
    private TangoPointCloudManager mPointCloudManager;
    private LearningEvaluator mLearningEvaluator;
    private TipManager mTipManager;
    private AdfScheduler mAdfScheduler;
    private AdfService mAdfService;
    private VisualContentManager mVisualContentManager;

    private TangoDriver mTangoDriver;
    private Map<Class, TangoState> tangoStates;
    private TangoStateConnectorFactory mConnectorFactory;


    public TangoDriverFactory(MainFactory mainFactory) {
        mConfig = mainFactory.getConfig();
        mTangoUpdater = mainFactory.getTangoUpdater();
        mTangoFactory = mainFactory.getTangoFactory();
        mRenderer = mainFactory.getRenderer();
        mPointCloudManager = mainFactory.getPointCloudManager();
        mLearningEvaluator = mainFactory.getLearningEvaluator();
        mTipManager = mainFactory.getTipManager();
        mAdfScheduler = mainFactory.getAdfScheduler();
        mAdfService = mainFactory.getAdfService();
        mVisualContentManager = mainFactory.getVisualContentManager();
        tangoStates = new HashMap<>();

        createTangoStates(mainFactory);
        mConnectorFactory = new TangoStateConnectorFactory(mTangoUpdater, mTangoDriver);
        createTangoStateConnectors();
    }

    public TangoDriver getTangoDriver() {
        return mTangoDriver;
    }

    public ContentFitter getContentFitter(Content c, ContentFitter.OnContentFitListener listener) {
        ContentFitter fitter = new ContentFitter(c, mTangoDriver, mVisualContentManager);
        fitter.addOnContentFitListener(listener);
        fitter.addOnContentFitListener(mTipManager);
        return fitter;
    }

    private void createTangoStates(MainFactory mainFactory) {
        long localizationTimeout = mConfig.getInt(TangoManagerConstants.LOCALIZATION_TIMEOUT);
        TangoForLearnedAdf tangoForLearnedAdf =
                new TangoForLearnedAdf(mTangoUpdater, mTangoFactory, mRenderer, mPointCloudManager);
        tangoForLearnedAdf.withLocalizationTimeout(localizationTimeout);
        tangoStates.put(TangoForLearnedAdf.class, tangoForLearnedAdf);

        TangoForLearning tangoForLearning =
                new TangoForLearning(mTangoUpdater, mTangoFactory, mRenderer, mLearningEvaluator, mPointCloudManager);
        tangoStates.put(TangoForLearning.class, tangoForLearning);

        TangoForReadyState tangoForReadyState =
                new TangoForReadyState(mTangoUpdater, mTangoFactory, mRenderer, mPointCloudManager);
        tangoStates.put(TangoForReadyState.class, tangoForReadyState);

        TangoForCloudAdfs tangoForCloudAdfs =
                new TangoForCloudAdfs(mTangoUpdater, mTangoFactory, mRenderer, mPointCloudManager, mAdfScheduler);
        tangoForCloudAdfs.withLocalizationTimeout(localizationTimeout);
        tangoStates.put(TangoForCloudAdfs.class, tangoForCloudAdfs);

        TangoForSavedAdf tangoForSavedAdf =
                new TangoForSavedAdf(mTangoUpdater, mTangoFactory, mRenderer, mAdfService, mPointCloudManager);
        tangoForSavedAdf.withLocalizationTimeout(localizationTimeout);
        tangoStates.put(TangoForSavedAdf.class, tangoForSavedAdf);

        setEventListener(mainFactory.getTangoUx());
        setEventListener(mainFactory.getTipManager());
        setEventListener(mainFactory.getReadyStateReporter());
        setEventListener((WallyEventListener) mainFactory.getActivity());

        mTangoUpdater.addTangoUpdaterListener(tangoForCloudAdfs);
        mTangoDriver = new TangoDriver(tangoForCloudAdfs);

        DisplayManager displayManager = (DisplayManager) mainFactory.getActivity().getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            mTangoDriver.setDefaultDisplay(mainFactory.getActivity().getWindowManager().getDefaultDisplay());
            displayManager.registerDisplayListener(mTangoDriver, null);
        }

//        mTangoUpdater.addTangoUpdaterListener(tangoForLearning);
//        mTangoDriver = new TangoDriver(tangoForLearning);
    }

    private void createTangoStateConnectors() {
        TangoForCloudAdfs tangoForCloudAdfs = (TangoForCloudAdfs) tangoStates.get(TangoForCloudAdfs.class);
        TangoStateConnector failConnector = mConnectorFactory.getConnectorFromCloudAdfsToLearning(
                tangoForCloudAdfs, (TangoForLearning) tangoStates.get(TangoForLearning.class));
        TangoStateConnector successConnector = mConnectorFactory.getConnectorFromCloudAdfsToReadyState(
                tangoForCloudAdfs, (TangoForReadyState) tangoStates.get(TangoForReadyState.class));
        tangoForCloudAdfs.withFailStateConnector(failConnector).withSuccessStateConnector(successConnector);


        TangoForLearnedAdf tangoForLearnedAdf = (TangoForLearnedAdf) tangoStates.get(TangoForLearnedAdf.class);
        failConnector = mConnectorFactory.getConnectorFromLearnedAdfToCloudAdf(
                tangoForLearnedAdf, (TangoForCloudAdfs) tangoStates.get(TangoForCloudAdfs.class));
        successConnector = mConnectorFactory.getConnectorFromLearnedAdfToReadyState(
                tangoForLearnedAdf, (TangoForReadyState) tangoStates.get(TangoForReadyState.class));
        tangoForLearnedAdf.withFailStateConnector(failConnector).withSuccessStateConnector(successConnector);

        TangoForLearning tangoForLearning = (TangoForLearning) tangoStates.get(TangoForLearning.class);
        failConnector = mConnectorFactory.getConnectorFromLearningToLearningState(
                tangoForLearning, (TangoForLearning) tangoStates.get(TangoForLearning.class));
        successConnector = mConnectorFactory.getConnectorFromLearningToLearnedAdf(
                tangoForLearning, (TangoForLearnedAdf) tangoStates.get(TangoForLearnedAdf.class));
        tangoForLearning.withFailStateConnector(failConnector).withSuccessStateConnector(successConnector);

        TangoForSavedAdf tangoForSavedAdf = (TangoForSavedAdf) tangoStates.get(TangoForSavedAdf.class);
        failConnector = mConnectorFactory.getConnectorFromSavedAdfCloudAdfState(
                tangoForSavedAdf, (TangoForCloudAdfs) tangoStates.get(TangoForCloudAdfs.class));
        successConnector = mConnectorFactory.getConnectorFromSavedAdfToReadyState(
                tangoForSavedAdf, (TangoForReadyState) tangoStates.get(TangoForReadyState.class));
        tangoForSavedAdf.withFailStateConnector(failConnector).withSuccessStateConnector(successConnector);


        TangoForReadyState tangoForReadyState = (TangoForReadyState) tangoStates.get(TangoForReadyState.class);
        failConnector = mConnectorFactory.getConnectorFromReadyToSavedAdfState(
                tangoForReadyState, (TangoForSavedAdf) tangoStates.get(TangoForSavedAdf.class));
        tangoForReadyState.withFailStateConnector(failConnector);
    }

    private void setEventListener(WallyEventListener eventListener) {
        for (TangoState tango : tangoStates.values()) {
            tango.addEventListener(eventListener);
        }
    }
}
