package com.wally.wally.tango;

import android.util.Log;

import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.wally.wally.tango.VisualContent.*;

/**
 * Created by shota on 5/30/16.
 */
public class VisualContentManager implements LocalizationListener{
    private static final String TAG = VisualContentManager.class.getSimpleName();
    private boolean mIsLocalized;
    private final Object mLocalizationLock = new Object();

    //Active Content
    private final Object mActiveContentLock = new Object();
    private ActiveVisualContent mActiveContent;

    //Static Content
    private final Object mStaticContentLock = new Object();
    private List<VisualContent> mStaticContent;

    //Selected Content
    private final Object mSelectedContentLock = new Object();
    private VisualContent mSelectedContent;
    private boolean mBorderOnScreen;


    public VisualContentManager(){
        mIsLocalized = false;
        mStaticContent = new ArrayList<>();
    }

    /**
     * Is called from TangoUpdater through LocalizationListener
     * indicates localized tango device
     */
    @Override
    public void localized() {
        synchronized (mLocalizationLock) {
            mIsLocalized = true;
        }
    }

    /**
     * Is called from TangoUpdater through LocalizationListener
     * indicates not localized tango device
     */
    @Override
    public void notLocalized() {
        synchronized (mLocalizationLock) {
            mIsLocalized = false;
        }
    }

    /**
     * Is called from Renderer thread
     * @return
     */
    private boolean isLocalized(){
        synchronized (mLocalizationLock) {
            return mIsLocalized;
        }
    }

    /****************************************** Active Content ******************************************/

    /**
     * Creates Active Content with @RenderStatus.PendingRender status. Is called from ContentFitter thread.
     * @param glPose
     * @param content
     */
    public void addPendingActiveContent(Pose glPose, Content content){
        Log.d(TAG, "addPendingActiveContent() called with: " + "glPose = [" + glPose + "], content = [" + content + "]");
        synchronized (mActiveContentLock) {
            content.withTangoData(new TangoData(glPose));
            this.mActiveContent = new ActiveVisualContent(content);
            mActiveContent.setStatus(RenderStatus.PendingRender);
        }
    }

    public void updateActiveContent(Pose newPose) {
        Log.d(TAG, "updateActiveContent() called with: " + "newPose = [" + newPose + "]");
        synchronized (mActiveContentLock) {
            if (mActiveContent != null) {
                mActiveContent.setNewPose(newPose);
            }
        }
    }

    public void removePendingActiveContent() {
        Log.d(TAG, "removePendingActiveContent() called with: " + "");
        synchronized (mActiveContentLock) {
            if (mActiveContent != null) {
                mActiveContent.setStatus(RenderStatus.PendingRemove);
            }
        }
    }

    /**
     * Is called when renderer renders active content
     */
    public void setActiveContentAdded(){
        Log.d(TAG, "setActiveContentAdded() called with: " + "");
        synchronized (mActiveContentLock) {
            mActiveContent.setStatus(RenderStatus.Rendered);
            //addPendingStaticContent(mActiveContent);
        }
    }

    public void setActiveContentFinishFitting(){
        synchronized (mActiveContentLock){
            synchronized (mStaticContentLock){
                int index = mStaticContent.indexOf(mActiveContent);
                if (index == -1) {
                    mStaticContent.add(mActiveContent);
                } else {
                    mStaticContent.set(index, mActiveContent);
                }
                mActiveContent = null;
            }
        }
    }

    public void setActiveContentRemoved(){
        Log.d(TAG, "setActiveContentRemoved() called with: " + "");
        synchronized (mActiveContentLock) {
            mActiveContent = null;
        }
    }

    public ActiveVisualContent getActiveContent() {
        Log.d(TAG, "getActiveContent() called with: " + "");
        synchronized (mActiveContentLock) {
            return mActiveContent;
        }
    }

    /**
     * Is called from Render thread. So renderer can decide to add active content
     * @return
     */
    public boolean shouldActiveContentRenderOnScreen() {
        Log.d(TAG, "shouldActiveContentRenderOnScreen() called with: " + "");
        synchronized (mActiveContentLock) {
            return mActiveContent != null && mActiveContent.getStatus() == RenderStatus.PendingRender;
        }
    }

    /**
     * Is called from Render thread. So renderer can decide to remove rendered active content
     * @return
     */
    public boolean shouldActiveContentRemoveFromScreen(){
        Log.d(TAG, "shouldActiveContentRemoveFromScreen() called with: " + "");
        synchronized (mActiveContentLock) {
            return mActiveContent != null && mActiveContent.getStatus() == RenderStatus.PendingRemove;
        }
    }




    /****************************************** Static Content ******************************************/

    public void addPendingStaticContent(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            visualContent.setStatus(RenderStatus.PendingRender);
            int index = mStaticContent.indexOf(visualContent);
            if (index == -1) {
                Log.d(TAG, "addPendingStaticContent() called with: " + "visualContent = [" + visualContent + "]");
                mStaticContent.add(visualContent);
            } else {
                mStaticContent.set(index, visualContent);
            }
        }
    }

    private void removePendingStaticContent(VisualContent visualContent){
        Log.d(TAG, "removePendingStaticContent() called with: " + "visualContent = [" + visualContent + "]");
        synchronized (mStaticContentLock){
            visualContent.setStatus(RenderStatus.PendingRemove);
            int index = mStaticContent.indexOf(visualContent);
            if (index == -1){
                Log.e(TAG, "removePendingStaticContent() called with: " + "visualContent = [" + visualContent + "] is not rendered");
            } else {
                mStaticContent.set(index, visualContent);
            }
        }
    }

    public void removePendingStaticContent(Content content) {
        Log.d(TAG, "removePendingStaticContent() called with: " + "content = [" + content + "]");
        synchronized (mStaticContentLock) {
            VisualContent vc = findVisualContentByContent(content);
            removePendingStaticContent(vc);
        }
    }

    private VisualContent findVisualContentByContent(Content content) {
        Log.d(TAG, "findVisualContentByContent() called with: " + "content = [" + content + "]");
        // TODO make with hashmap to get better performance
        synchronized (mStaticContentLock) {
            for (VisualContent vc : mStaticContent) {
                if (vc.getContent().equals(content)) {
                    return vc;
                }
            }
            return null;
        }
    }

    public void setStaticContentAdded(VisualContent visualContent){
        Log.d(TAG, "setStaticContentAdded() called with: " + "visualContent = [" + visualContent + "]");
        synchronized (mStaticContentLock){
            visualContent.setStatus(RenderStatus.Rendered);
            int index = mStaticContent.indexOf(visualContent);
            if (index == -1){
                Log.d(TAG, "setStaticContentAdded() called with: " + "visualContent = [" + visualContent + "] is not in the list");
            } else {
                mStaticContent.set(index, visualContent);
            }
        }
    }

    public void setStaticContentRemoved(VisualContent visualContent){
        Log.d(TAG, "setStaticContentRemoved() called with: " + "visualContent = [" + visualContent + "]");
        synchronized (mStaticContentLock){
            int index = mStaticContent.indexOf(visualContent);
            if (index == -1){
                Log.d(TAG, "setStaticContentRemoved() called with: " + "visualContent = [" + visualContent + "] is not in the list");
            } else {
                mStaticContent.remove(visualContent);
            }
        }
    }

    public Iterator<VisualContent> getStaticVisualContentToAdd(){
        Log.d(TAG, "getStaticVisualContentToAdd() called with: " + "");
        synchronized (mStaticContentLock) {
            return filterStaticVisualContentList(RenderStatus.PendingRender);
        }
    }

    public Iterator<VisualContent> getStaticVisualContentToRemove(){
        Log.d(TAG, "getStaticVisualContentToRemove() called with: " + "");
        synchronized (mStaticContentLock) {
            return filterStaticVisualContentList(RenderStatus.PendingRemove);
        }
    }

    private Iterator<VisualContent> filterStaticVisualContentList(RenderStatus status){
        List<VisualContent> res = new ArrayList<>();
        for (VisualContent vc : mStaticContent){
            if(vc.getStatus() == status){
                res.add(vc);
            }
        }
        return res.iterator();
    }

    public void createStaticContent(final Collection<Content> collection){
        Log.d(TAG, "createStaticContent() called with: " + "collection = [" + collection + "]");
        synchronized (mStaticContentLock) {
            for (Content c : collection) {
                addPendingStaticContent(new VisualContent(c));
            }
        }
    }

    public VisualContent findContentByObject3D(Object3D object) {
        // TODO make with hashmap to get better performance
        synchronized (mStaticContentLock) {
            for (VisualContent vc : mStaticContent) {
                if (vc.getVisual().equals(object) && vc.getStatus() == RenderStatus.Rendered) {
                    return vc;
                }
            }
        }
        return null;
    }

    /****************************************** Selected Content ******************************************/

    public void setSelectedContent(VisualContent visualContent) {
        synchronized (mSelectedContentLock) {
            mSelectedContent = visualContent;
        }
    }

    public boolean isSelectedContent(VisualContent visualContent) {
        synchronized (mSelectedContentLock) {
            return visualContent == mSelectedContent;
        }
    }

    public boolean isBorderOnScreen() {
        synchronized (mSelectedContentLock) {
            return mBorderOnScreen;
        }
    }

    public void setBorderOnScreen(boolean borderOnScreen) {
        synchronized (mSelectedContentLock) {
            this.mBorderOnScreen = borderOnScreen;
        }
    }

}
