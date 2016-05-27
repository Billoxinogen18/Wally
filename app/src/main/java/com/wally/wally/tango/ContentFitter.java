package com.wally.wally.tango;

import android.os.AsyncTask;
import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

/**
 * Thread that fits content on the wall.
 * <p/>
 * Note that You must cancel this when using in activity, because it will run without stop!
 * <br/>
 * If you do not cancel in onPause, you will get Memory Leak.
 * <p/>
 * Created by ioane5 on 4/26/16.
 */
public class ContentFitter extends AsyncTask<Void, TangoPoseData, Void> {

    private TangoManager mTangoManager;
    private Content mContent;
    private OnContentFitListener mFittingStatusListener;
    private TangoPoseData lastPose;
    private VisualContentManager mVisualContentManager;

    public ContentFitter(Content content, TangoManager tangoManager, VisualContentManager visualContentManager) {
        mContent = content;
        mTangoManager = tangoManager;
        mVisualContentManager = visualContentManager;
    }

    public Content getContent() {
        return mContent;
    }

    public void setFittingStatusListener(OnContentFitListener fittingStatusListener) {
        this.mFittingStatusListener = fittingStatusListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        TangoPoseData tangoPoseData = getValidPose();
        while (tangoPoseData == null) {
            tangoPoseData = getValidPose();
            mFittingStatusListener.onContentFit(null);
            if (isCancelled()) {
                return null;
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        mFittingStatusListener.onContentFit(null);
        mVisualContentManager.createActiveContent(ScenePoseCalculator.toOpenGLPose(tangoPoseData), getContent());

        // Update content timely, while we are cancelled.
        while (true) {
            if (isCancelled()) {
                break;
            }
            try {
                Thread.sleep(400);
                publishProgress(getValidPose());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(TangoPoseData... newPoses) {
        super.onProgressUpdate(newPoses);
        TangoPoseData newPose = (newPoses != null && newPoses.length > 0) ? newPoses[0] : null;
        if (newPose != null && newPose.statusCode == TangoPoseData.POSE_INVALID) {
            newPose = null;
        }
        mFittingStatusListener.onContentFit(newPose);
        lastPose = newPose;
        if (newPose != null) {
            mVisualContentManager.updateActiveContent(ScenePoseCalculator.toOpenGLPose(newPose));
        }

    }

    public TangoPoseData getPose() {
        return lastPose;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mTangoManager.removeActiveContent();
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        mTangoManager.removeActiveContent();
    }

    public void finishFitting() {
        // Order of this calls matter!!!
        mFittingStatusListener.onContentFittingFinished(getContent());
        addActiveToStaticContent();
        cancel(true);
    }

    /**
     * Tries to get valid plane pose, null if interrupted.
     */
    private TangoPoseData getValidPose() {
        if (mTangoManager.isConnected()) {
            TangoPoseData tangoPose;
            try {
                tangoPose = mTangoManager.findPlaneInMiddle();
                if (tangoPose != null) {
                    return tangoPose;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void addActiveToStaticContent() {
        mVisualContentManager.activeContentAddingFinished();
        mTangoManager.removeActiveContent();
    }



    public interface OnContentFitListener {
        void onContentFit(TangoPoseData pose);

        void onFitStatusChange(boolean fittingStarted);

        void onContentFittingFinished(Content content);
    }

}

