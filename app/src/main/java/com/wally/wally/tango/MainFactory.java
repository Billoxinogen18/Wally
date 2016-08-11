package com.wally.wally.tango;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.App;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.controllers.main.TipManager;
import com.wally.wally.controllers.main.TipView;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.renderer.ActiveContentScaleGestureDetector;
import com.wally.wally.renderer.OnVisualContentSelectedListener;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.states.TangoBase;
import com.wally.wally.tango.states.TangoForCloudAdfs;
import com.wally.wally.tango.states.TangoForLearnedAdf;
import com.wally.wally.tango.states.TangoForLearning;
import com.wally.wally.tango.states.TangoForReadyState;
import com.wally.wally.tango.states.TangoForSavedAdf;
import com.wally.wally.tip.LocalTipService;
import com.wally.wally.tip.TipService;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Main Factory reponsible for creating Tango Managers
 */
public class MainFactory {
    private TangoUpdater mTangoUpdater;
    private VisualContentManager mVisualContentManager;
    private TangoFactory mTangoFactory;
    private Config mConfig;
    private TangoPointCloudManager mPointCloudManager;
    private WallyRenderer mRenderer;
    private LearningEvaluator mLearningEvaluator;
    private TipManager mTipManager;
    private AdfScheduler mAdfScheduler;
    private final ScaleGestureDetector mScaleDetector;
    private TangoDriver mTangoDriver;

    private Map<Class, TangoBase> tangoManagers;
    private WallyTangoUx mTangoUx;

    public MainFactory(Context context,
                       TipView tipView,
                       TangoUxLayout tangoUxLayout,
                       RajawaliSurfaceView surfaceView,
                       TangoUpdater.TangoUpdaterListener tangoUpdaterListener,
                       OnVisualContentSelectedListener onContentSelectedListener) {
        set();

        mTangoUx = new WallyTangoUx(context);
        mTangoUx.setLayout(tangoUxLayout);

        mTangoUpdater = new TangoUpdater(mTangoUx, surfaceView, mPointCloudManager);
        mTangoUpdater.addTangoUpdaterListener(tangoUpdaterListener);

        mScaleDetector = new ScaleGestureDetector(context,
                new ActiveContentScaleGestureDetector(mVisualContentManager));

        mRenderer = new WallyRenderer(context, mVisualContentManager, onContentSelectedListener);
        surfaceView.setSurfaceRenderer(mRenderer);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                mRenderer.onTouchEvent(event);
                return true;
            }
        });


        mTangoFactory = new TangoFactory(context);

        TipService tipService = new LocalTipService(Utils.getAssetContentAsString(context, "tips.json"));
        mTipManager = new TipManager(tipView, tipService);

        createTangoManagers();
    }


    private void set() {
        tangoManagers = new HashMap<>();

        mConfig = Config.getInstance();
        mLearningEvaluator = new LearningEvaluator(mConfig);

        mPointCloudManager = new TangoPointCloudManager();

        mVisualContentManager = new VisualContentManager();

        AdfManager mAdfManager = App.getInstance().getAdfManager();
        mAdfScheduler = new AdfScheduler(mAdfManager);

        ProgressAggregator mProgressAggregator = new ProgressAggregator();
        mProgressAggregator.addProgressReporter(mAdfScheduler, 0.4);
        mProgressAggregator.addProgressReporter(mLearningEvaluator, 0.6);
    }


    public TangoBase getTangoManager(Class cl) {
        return tangoManagers.get(cl);
    }

    private void createTangoManagers() {
        long localizationTimeout = mConfig.getInt(TangoManagerConstants.LOCALIZATION_TIMEOUT);
        TangoForLearnedAdf tangoForLearnedAdf =
                new TangoForLearnedAdf(mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoForLearnedAdf.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForLearnedAdf.class, tangoForLearnedAdf);

        TangoForLearning tangoForLearning =
                new TangoForLearning(mTangoUpdater, mTangoFactory, mRenderer, mLearningEvaluator, tangoManagers, mPointCloudManager);
        tangoManagers.put(TangoForLearning.class, tangoForLearning);

        TangoForReadyState tangoForReadyState =
                new TangoForReadyState(mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoManagers.put(TangoForReadyState.class, tangoForReadyState);

        TangoForCloudAdfs tangoForCloudAdfs =
                new TangoForCloudAdfs(mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager, mAdfScheduler);
        tangoForCloudAdfs.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForCloudAdfs.class, tangoForCloudAdfs);

        TangoForSavedAdf tangoForSavedAdf =
                new TangoForSavedAdf(mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoForSavedAdf.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForSavedAdf.class, tangoForSavedAdf);

        setEventListener(mTipManager);

        mTangoUpdater.addTangoUpdaterListener(tangoForCloudAdfs);
        mTangoDriver = new TangoDriver(tangoForCloudAdfs);
    }

    public TangoDriver getTangoDriver() {
        return mTangoDriver;
    }

    public void setEventListener(TipManager eventListener) {
        for (TangoBase tango : tangoManagers.values()) {
            tango.addEventListener(eventListener);
        }
    }

    public VisualContentManager getVisualContentManager() {
        return mVisualContentManager;
    }

    public WallyTangoUx getTangoUx() {
        return mTangoUx;
    }

    public ContentFitter getContentFitter(Content c, ContentFitter.OnContentFitListener listener) {
        ContentFitter fitter = new ContentFitter(c, mTangoDriver, mVisualContentManager);
        fitter.addOnContentFitListener(listener);
        fitter.addOnContentFitListener(mTipManager);
        return fitter;
    }
}
