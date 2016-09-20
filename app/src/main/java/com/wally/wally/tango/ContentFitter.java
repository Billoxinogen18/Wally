package com.wally.wally.tango;

import android.os.AsyncTask;
import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.renderer.VisualContentManager;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread that fits content on the wall.
 * <p/>
 * Note that You must cancel this when using in activity, because it will run without stop!
 * <br/>
 * If you do not cancel in onPause, you will get Memory Leak.
 * <p/>
 * Created by ioane5 on 4/26/16.
 */
public class ContentFitter extends AsyncTask<Void, float[], Void> {
    private static final String TAG = ContentFitter.class.getSimpleName();

    private TangoDriver mTangoDriver;
    private Content mContent;
    private List<OnContentFitListener> mOnContentFitListeners = new ArrayList<>();
    private float[] lastPose;
    private VisualContentManager mVisualContentManager;

    public ContentFitter(Content content, TangoDriver tangoManager, VisualContentManager visualContentManager) {
        Log.d(TAG, "ContentFitter() called with: " + "content = [" + content + "], tangoManager = [" + tangoManager + "], visualContentManager = [" + visualContentManager + "]");
        mContent = content;
        mTangoDriver = tangoManager;
        mVisualContentManager = visualContentManager;
    }

    public void addOnContentFitListener(OnContentFitListener onContentFitListener){
        mOnContentFitListeners.add(onContentFitListener);
    }

    public Content getContent() {
        return mContent;
    }


    @Override
    protected Void doInBackground(Void... params) {
        Pose tangoPoseData = null;
        while (tangoPoseData == null) {
            try {
                tangoPoseData = mTangoDriver.getDevicePoseInFront();
            }catch (Exception e){
                tangoPoseData = null;
            }
            for (OnContentFitListener onContentFitListener : mOnContentFitListeners) {
                onContentFitListener.onContentFit(null);
            }
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
        for (OnContentFitListener onContentFitListener : mOnContentFitListeners) {
            onContentFitListener.onContentFit(null);
        }
        mVisualContentManager.addPendingActiveContent(tangoPoseData, getContent());

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
    protected void onProgressUpdate(float[]... newPoses) {
        super.onProgressUpdate(newPoses);
        float[] newPose = (newPoses != null && newPoses.length > 0) ? newPoses[0] : null;
//        if (newPose != null && newPose.statusCode == TangoPoseData.POSE_INVALID) {
//            newPose = null;
//        }
        for (OnContentFitListener onContentFitListener : mOnContentFitListeners) {
            onContentFitListener.onContentFit(newPose);
        }
        lastPose = newPose;
        if (mVisualContentManager.isActiveContent()) { //TODO cancel content fitter when not localized
            if (newPose != null) {
                Matrix4 m = new Matrix4(newPose);
                Pose p = new Pose(m.getTranslation(), new Quaternion().fromMatrix(m).conjugate());
                mVisualContentManager.updateActiveContent(p);
            } else {
                mVisualContentManager.updateActiveContent(mTangoDriver.getDevicePoseInFront());
            }
        }

    }

    public float[] getPose() {
        return lastPose;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(TAG, "onCancelled()");
        if(mVisualContentManager.isActiveContent()) {
            mVisualContentManager.removePendingActiveContent();
        }
        mVisualContentManager.removeSavedActiveContent();
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        //mVisualContentManager.removePendingActiveContent();
    }

    public void finishFitting() {
        Log.d(TAG, "finishFitting()");
        // Order of this calls matter!!!
        for (OnContentFitListener onContentFitListener : mOnContentFitListeners) {
            onContentFitListener.onContentFittingFinished(getContent());
        }
        //mVisualContentManager.setActiveContentAdded();
        // TODO here we might lost localization (Theoretically possible)
        mVisualContentManager.setActiveContentFinishFitting();
//        if(mVisualContentManager.isActiveContent()) {
//            mVisualContentManager.removePendingActiveContent();
//        }
        cancel(true);
    }

    /**
     * Tries to get valid plane pose, null if interrupted.
     */
    private float[] getValidPose() {
        if (mTangoDriver.isTangoConnected()) {
            float[] tangoPose;
            try {
                tangoPose = mTangoDriver.findPlaneInMiddle();
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
        void onContentFit(float[] pose);

        void onFitStatusChange(boolean fittingStarted);

        void onContentFittingFinished(Content content);
    }

}

