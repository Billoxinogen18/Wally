package com.wally.wally.tango;

import android.os.AsyncTask;
import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.datacontroller.content.Content;

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

    public ContentFitter(Content content, TangoManager tangoManager) {
        mContent = content;
        mTangoManager = tangoManager;
    }

    public Content getContent() {
        return mContent;
    }

    public double getScale() {

        if (mTangoManager == null) {
            Log.d("bla", "tango");
        }

        if (mTangoManager.getVisualContentManager() == null) {
            Log.d("bla", "visualcontentmanager");
        }

        if (mTangoManager.getVisualContentManager().getActiveContent() == null) {
            Log.d("bla", "active content");
        }

        if (mTangoManager.getVisualContentManager().getActiveContent().getScale() == null) {
            Log.d("bla", "scalse");
        }
        return mTangoManager.getVisualContentManager().getActiveContent().getScale().x;
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
        Log.d("BLS", "Setting activecontent to tango... " + getContent());
        mTangoManager.setActiveContent(tangoPoseData, getContent());

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
            mTangoManager.updateActiveContent(newPose);
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
        mFittingStatusListener.onContentFittingFinished(getContent(), getPose(), getScale());
        mTangoManager.addActiveToStaticContent();
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

    public interface OnContentFitListener {
        void onContentFit(TangoPoseData pose);

        void onFitStatusChange(boolean fittingStarted);

        void onContentFittingFinished(Content content, TangoPoseData pose, double scale);
    }

}

