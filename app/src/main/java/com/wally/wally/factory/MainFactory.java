package com.wally.wally.factory;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.App;
import com.wally.wally.Utils;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.config.Config;
import com.wally.wally.config.TangoManagerConstants;
import com.wally.wally.controllers.main.CameraARTangoActivity;
import com.wally.wally.controllers.main.TipManager;
import com.wally.wally.controllers.main.TipView;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.renderer.ActiveContentScaleGestureDetector;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.ContentFitter;
import com.wally.wally.tango.LearningEvaluator;
import com.wally.wally.tango.ProgressAggregator;
import com.wally.wally.tango.TangoDriver;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tango.states.TangoForCloudAdfs;
import com.wally.wally.tango.states.TangoForLearnedAdf;
import com.wally.wally.tango.states.TangoForLearning;
import com.wally.wally.tango.states.TangoForReadyState;
import com.wally.wally.tango.states.TangoForSavedAdf;
import com.wally.wally.tango.states.TangoState;
import com.wally.wally.tip.LocalTipService;
import com.wally.wally.tip.TipService;
import com.wally.wally.ux.WallyTangoUx;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shota on 8/9/16.
 * Main Factory responsible for creating Tango Managers
 */
public class MainFactory {
    private ScaleGestureDetector mScaleDetector;
    private TangoUpdater mTangoUpdater;
    private VisualContentManager mVisualContentManager;
    private TangoFactory mTangoFactory;
    private Config mConfig;
    private TangoPointCloudManager mPointCloudManager;
    private WallyRenderer mRenderer;
    private LearningEvaluator mLearningEvaluator;
    private TipManager mTipManager;
    private AdfScheduler mAdfScheduler;
    private TangoDriver mTangoDriver;

    private Map<Class, TangoState> tangoManagers;
    private WallyTangoUx mTangoUx;
    private TangoState.Executor mExecutor;

    public MainFactory(TipView tipView,
                       TangoUxLayout tangoUxLayout,
                       CameraARTangoActivity activity,
                       RajawaliSurfaceView surfaceView) {
        init();
        mExecutor = createExecutor(activity);
        Context context = activity.getBaseContext();
        mTangoUx = new WallyTangoUx(context, mConfig);
        mTangoUx.setLayout(tangoUxLayout);

        mTangoUpdater = new TangoUpdater(mTangoUx, surfaceView, mPointCloudManager);
        mTangoUpdater.addTangoUpdaterListener(activity);

        mScaleDetector = new ScaleGestureDetector(context,
                new ActiveContentScaleGestureDetector(mVisualContentManager));

        mRenderer = new WallyRenderer(context, mVisualContentManager, activity);
        surfaceView.setSurfaceRenderer(mRenderer);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                mRenderer.onTouchEvent(event);
                return true;
            }
        });

        ProgressAggregator progressAggregator = new ProgressAggregator();
        progressAggregator.addProgressReporter(mAdfScheduler, 0.3f);
        progressAggregator.addProgressReporter(mLearningEvaluator, 0.7f);
        progressAggregator.addProgressListener(activity);

        mTangoFactory = new TangoFactory(context);

        TipService tipService = new LocalTipService(
                Utils.getAssetContentAsString(context, "tips.json"),
                context.getSharedPreferences("tips", Context.MODE_PRIVATE));
        mTipManager = new TipManager(tipView, tipService);

        createTangoManagers();
    }

    private void init() {
        tangoManagers = new HashMap<>();

        mConfig = Config.getInstance();
        mLearningEvaluator = new LearningEvaluator(mConfig);

        mPointCloudManager = new TangoPointCloudManager();

        mVisualContentManager = new VisualContentManager();
        mAdfScheduler = new AdfScheduler(App.getInstance().getAdfManager());
    }

    private void createTangoManagers() {
        long localizationTimeout = mConfig.getInt(TangoManagerConstants.LOCALIZATION_TIMEOUT);
        TangoForLearnedAdf tangoForLearnedAdf =
                new TangoForLearnedAdf(mExecutor, mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoForLearnedAdf.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForLearnedAdf.class, tangoForLearnedAdf);

        TangoForLearning tangoForLearning =
                new TangoForLearning(mExecutor, mTangoUpdater, mTangoFactory, mRenderer, mLearningEvaluator, tangoManagers, mPointCloudManager);
        tangoManagers.put(TangoForLearning.class, tangoForLearning);

        TangoForReadyState tangoForReadyState =
                new TangoForReadyState(mExecutor, mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoManagers.put(TangoForReadyState.class, tangoForReadyState);

        TangoForCloudAdfs tangoForCloudAdfs =
                new TangoForCloudAdfs(mExecutor, mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager, mAdfScheduler);
        tangoForCloudAdfs.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForCloudAdfs.class, tangoForCloudAdfs);

        TangoForSavedAdf tangoForSavedAdf =
                new TangoForSavedAdf(mExecutor, mTangoUpdater, mTangoFactory, mRenderer, tangoManagers, mPointCloudManager);
        tangoForSavedAdf.withLocalizationTimeout(localizationTimeout);
        tangoManagers.put(TangoForSavedAdf.class, tangoForSavedAdf);

        setEventListener(mTipManager);
        setEventListener(mTangoUx);

        mTangoUpdater.addTangoUpdaterListener(tangoForCloudAdfs);
        mTangoDriver = new TangoDriver(tangoForCloudAdfs);

//        mTangoUpdater.addTangoUpdaterListener(tangoForLearning);
//        mTangoDriver = new TangoDriver(tangoForLearning);
    }

    public TangoDriver getTangoDriver() {
        return mTangoDriver;
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

    private void setEventListener(WallyEventListener eventListener) {
        for (TangoState tango : tangoManagers.values()) {
            tango.addEventListener(eventListener);
        }
    }

    private TangoState.Executor createExecutor(final Activity activity) {
        return new TangoState.Executor() {
            @Override
            public void execute(Runnable runnable) {
                //new Thread(runnable).start();
                activity.runOnUiThread(runnable);
            }
        };
    }
}
