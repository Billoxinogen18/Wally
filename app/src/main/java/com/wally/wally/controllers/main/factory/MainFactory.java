package com.wally.wally.controllers.main.factory;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.atap.tango.ux.TangoUxLayout;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.App;
import com.wally.wally.adf.AdfScheduler;
import com.wally.wally.config.Config;
import com.wally.wally.controllers.main.CameraARTangoActivity;
import com.wally.wally.events.WallyEvent;
import com.wally.wally.events.WallyEventListener;
import com.wally.wally.progressReporter.ProgressListener;
import com.wally.wally.progressReporter.ProgressReporter;
import com.wally.wally.renderer.ActiveContentScaleGestureDetector;
import com.wally.wally.renderer.VisualContentManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.tango.LearningEvaluator;
import com.wally.wally.progressReporter.ProgressAggregator;
import com.wally.wally.tango.TangoFactory;
import com.wally.wally.tango.TangoUpdater;
import com.wally.wally.tip.LocalTipService;
import com.wally.wally.tip.TipManager;
import com.wally.wally.tip.TipService;
import com.wally.wally.tip.TipView;
import com.wally.wally.ux.WallyTangoUx;

import org.rajawali3d.surface.RajawaliSurfaceView;


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

    private WallyTangoUx mTangoUx;
    private Activity activity;

    public MainFactory(TipView tipView,
                       TangoUxLayout tangoUxLayout,
                       CameraARTangoActivity activity,
                       RajawaliSurfaceView surfaceView) {
        init();
        this.activity = activity;
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
        ReadyStateReporter readyStateReporter = new ReadyStateReporter();
        progressAggregator.addProgressReporter(readyStateReporter, 0.1f);
        progressAggregator.addProgressReporter(mAdfScheduler, 0.3f);
        progressAggregator.addProgressReporter(mLearningEvaluator, 0.6f);
        progressAggregator.addProgressListener(activity);

        mTangoFactory = new TangoFactory(context);

        TipService tipService = LocalTipService.getInstance(context);
        mTipManager = new TipManager(tipView, tipService);

    }

    private void init() {
        mConfig = Config.getInstance();
        mLearningEvaluator = new LearningEvaluator(mConfig);

        mPointCloudManager = new TangoPointCloudManager();

        mVisualContentManager = new VisualContentManager();
        mAdfScheduler = new AdfScheduler(App.getInstance().getAdfManager());
    }

    public VisualContentManager getVisualContentManager() {
        return mVisualContentManager;
    }

    public WallyTangoUx getTangoUx() {
        return mTangoUx;
    }

    public TangoUpdater getTangoUpdater() {
        return mTangoUpdater;
    }

    public TangoFactory getTangoFactory() {
        return mTangoFactory;
    }

    public Config getConfig() {
        return mConfig;
    }

    public TangoPointCloudManager getPointCloudManager() {
        return mPointCloudManager;
    }

    public WallyRenderer getRenderer() {
        return mRenderer;
    }

    public LearningEvaluator getLearningEvaluator() {
        return mLearningEvaluator;
    }

    public TipManager getTipManager() {
        return mTipManager;
    }

    public AdfScheduler getAdfScheduler(){
        return mAdfScheduler;
    }

    public Activity getActivity() {
        return activity;
    }


    public class ReadyStateReporter implements WallyEventListener, ProgressReporter {
        private ProgressListener listener;

        @Override
        public void addProgressListener(ProgressListener listener) {
            this.listener = listener;
        }

        @Override
        public void onWallyEvent(WallyEvent event) {
            if (WallyEvent.TANGO_READY.equals(event.getId())) {
                listener.onProgressUpdate(this, 1);
            }
        }
    }
}
