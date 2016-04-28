package com.wally.wally.tango;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.atap.tangoservice.TangoPoseData;
import com.wally.wally.Utils;
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
    private Context mContext;
    private FittingStatusListener mFittingStatusListener;
    private TangoPoseData lastPose;

    public ContentFitter(Context context, Content content, TangoManager tangoManager) {
        mContext = context;
        mContent = content;
        mTangoManager = tangoManager;
    }

    public Content getContent() {
        return mContent;
    }

    public double getScale() {
        return mTangoManager.getVisualContentManager().getActiveContent().getScale();
    }

    public void setFittingStatusListener(FittingStatusListener fittingStatusListener) {
        this.mFittingStatusListener = fittingStatusListener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Bitmap bitmap = Utils.createBitmapFromContent(mContent, mContext);

        TangoPoseData tangoPoseData = getValidPose();
        while (tangoPoseData == null) {
            tangoPoseData = getValidPose();
            mFittingStatusListener.onContentFit(null);
            if (isCancelled()) {
                return null;
            }
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        mFittingStatusListener.onContentFit(null);
        mTangoManager.setActiveContent(bitmap, tangoPoseData, getContent());

        // Update content timely, while we are cancelled.
        while (true) {
            if (isCancelled()) {
                break;
            }
            try {
                Thread.sleep(600);
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

    public interface FittingStatusListener {

        /**
         * @param pose if the plane is valid and content can be placed. or null if invalid
         */
        void onContentFit(TangoPoseData pose);
    }
}

