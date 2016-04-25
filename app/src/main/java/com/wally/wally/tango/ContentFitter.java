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
public class ContentFitter extends AsyncTask<Void, TangoPoseData, Boolean> {

    private TangoManager mTangoManager;
    private Content mContent;
    private Context mContext;
    private FittingStatusListener mFittingStatusListener;

    public ContentFitter(Context context, Content content, TangoManager tangoManager) {
        mContext = context;
        mContent = content;
        mTangoManager = tangoManager;
    }

    public Content getContent() {
        return mContent;
    }

    public void setFittingStatusListener(FittingStatusListener fittingStatusListener) {
        this.mFittingStatusListener = fittingStatusListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Bitmap bitmap = Utils.createBitmapFromContent(mContent, mContext);

        TangoPoseData tangoPoseData = getValidPose();
        if (tangoPoseData == null) {
            return false;
        }
        mTangoManager.setActiveContent(bitmap, tangoPoseData);
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
        return true;
    }

    @Override
    protected void onProgressUpdate(TangoPoseData... newPose) {
        super.onProgressUpdate(newPose);
        if (mTangoManager.isConnected()) {
            mFittingStatusListener.onContentFit(newPose != null);
        } else {
            mFittingStatusListener.onContentFitError();
        }

        if (newPose != null) {
            mTangoManager.updateActiveContent(newPose[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success == null || !success) {
            mFittingStatusListener.onAbortFitting();
        }
        mTangoManager.removeActiveContent();
    }

    /**
     * Tries to get valid plane pose, null if interrupted.
     */
    private TangoPoseData getValidPose() {
        while (true) {
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
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public interface FittingStatusListener {

        /**
         * @param isValidPlane if the plane is valid and content can be placed.
         */
        void onContentFit(boolean isValidPlane);

        /**
         * Something bad happened, maybe TangoManager has lost contact or something.
         */
        void onContentFitError();

        /**
         * Aborted fitting, show an error to the user.
         */
        void onAbortFitting();
    }
}

