package com.wally.wally.renderer;

import android.util.Log;

import com.projecttango.rajawali.Pose;
import com.wally.wally.Utils;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.TangoData;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shota on 5/30/16.
 */
public class VisualContentManager {
    private static final String TAG = VisualContentManager.class.getSimpleName();
    private final Object mLocalizationLock = new Object();
    //Active Content
    private final Object mActiveContentLock = new Object();
    //Static Content
    private final Object mStaticContentLock = new Object();
    //Selected Content
    private final Object mSelectedContentLock = new Object();
    private boolean mIsLocalized;
    private ActiveVisualContent mActiveContent;
    private ActiveVisualContent mSavedActiveContent;
    private List<VisualContent> mStaticContent;
    private List<VisualContent> mSavedStaticContent;
    private VisualContent mSelectedContent;
    private boolean mBorderOnScreen;


    public VisualContentManager() {
        mIsLocalized = false;
        mStaticContent = new ArrayList<>();
        mSavedStaticContent = new ArrayList<>();
    }

    /**
     * Is called from TangoUpdater through LocalizationListener
     * indicates localized tango device
     */
    public void visualContentRestoreAndShow() {
        Log.d(TAG, "visualContentRestoreAndShow() called with: " + "");
        synchronized (mLocalizationLock) {
            mIsLocalized = true;
            synchronized (mActiveContentLock){
                if (mSavedActiveContent != null) {
                    mActiveContent = getLocalizationNewActiveContent(mSavedActiveContent, mActiveContent);
                    mSavedActiveContent = null;
                }
            }
            synchronized (mStaticContentLock) {
                if (mSavedStaticContent != null) {
                    mStaticContent = getLocalizationNewStaticContent(mSavedStaticContent, mStaticContent);
                    mSavedStaticContent = null;
                }
            }
        }
    }

    private ActiveVisualContent getLocalizationNewActiveContent(ActiveVisualContent savedContent, ActiveVisualContent contentNow){
        if ((savedContent.getStatus() == VisualContent.RenderStatus.Rendered || savedContent.getStatus() == VisualContent.RenderStatus.PendingRender)
                && (contentNow == null || (contentNow.getStatus() != VisualContent.RenderStatus.Rendered && contentNow.getStatus() != VisualContent.RenderStatus.PendingRemove))) {
            savedContent.setStatus(VisualContent.RenderStatus.PendingRender);
        }

        return savedContent;
    }

    /**
     * Is called from TangoUpdater through LocalizationListener
     * indicates not localized tango device
     */
    public void visualContentSaveAndClear() {
        Log.d(TAG, "notLocalized() called with: " + "");
        synchronized (mLocalizationLock) {
            mIsLocalized = false;
            synchronized (mActiveContentLock){
                if (isActiveContent()) {
                    mSavedActiveContent = mActiveContent.cloneContent();
                    removePendingActiveContent();
                }
            }
            synchronized (mStaticContentLock) {
                mSavedStaticContent = cloneList(mStaticContent);
                removeAllStaticContent();
            }
        }
    }

    /**
     * Is called from Renderer thread
     *
     * @return
     */
    public boolean isLocalized() {
        synchronized (mLocalizationLock) {
            return mIsLocalized;
        }
    }

    private List<VisualContent> cloneList(List<VisualContent> list) {
        List<VisualContent> res = new ArrayList<>();
        for (VisualContent vc : list) {
            res.add(vc.cloneContent());
        }
        return res;
    }


    private VisualContent findVisualContentInStaticContentList(VisualContent vc, List<VisualContent> contentNow){
        for(VisualContent c: contentNow){
            if (c.getContent().equals(vc.getContent())) return c;
        }
        return null;
    }

    private ArrayList<VisualContent> getLocalizationNewStaticContent(List<VisualContent> savedContent, List<VisualContent> contentNow){
        ArrayList<VisualContent> newCon = new ArrayList<>();
        for (VisualContent oldC : savedContent){
            VisualContent newC = findVisualContentInStaticContentList(oldC, contentNow);
            if (newC != null){
                if((oldC.getStatus() == VisualContent.RenderStatus.Rendered || oldC.getStatus() == VisualContent.RenderStatus.PendingRender) &&
                        (newC.getStatus() != VisualContent.RenderStatus.Rendered && newC.getStatus() != VisualContent.RenderStatus.PendingRemove)){
                    oldC.setStatus(VisualContent.RenderStatus.PendingRender);
                    newCon.add(oldC);
                } else if (oldC.getStatus() == VisualContent.RenderStatus.PendingRemove && newC.getStatus() != VisualContent.RenderStatus.None){
                    newCon.add(oldC);
                }
            } else {
                if (oldC.getStatus() == VisualContent.RenderStatus.Rendered){
                    oldC.setStatus(VisualContent.RenderStatus.PendingRender);
                    newCon.add(oldC);
                } else if (oldC.getStatus() == VisualContent.RenderStatus.PendingRender){
                    newCon.add(oldC);
                }
            }
        }
        return newCon;
    }

    private void removeAllStaticContent() {
        //TODO buggy when renderer gets pending staticContent it will be rendered anyway.
        synchronized (mStaticContentLock) {
            for (VisualContent vc : mStaticContent) {
                if (vc.getStatus() == VisualContent.RenderStatus.Rendered) {
                    vc.setStatus(VisualContent.RenderStatus.PendingRemove);
                } else if (vc.getStatus() == VisualContent.RenderStatus.PendingRender) {
                    vc.setStatus(VisualContent.RenderStatus.None);
                }
            }
        }
    }

    /****************************************** Active Content ******************************************/

    /**
     * Creates Active Content with @RenderStatus.PendingRender status. Is called from ContentFitter thread.
     *
     * @param glPose
     * @param content
     */
    public void addPendingActiveContent(Pose glPose, Content content) {
        synchronized (mActiveContentLock) {
            if (mActiveContent == null) {
                TangoData td = new TangoData(glPose);
                if (content != null && content.getTangoData() != null) {
                    td = td.withScale(content.getTangoData().getScale());
                }
                content.withTangoData(td);
                this.mActiveContent = new ActiveVisualContent(content);
                mActiveContent.setStatus(VisualContent.RenderStatus.PendingRender);
            } else {
                Utils.throwError();
            }
        }
    }

    public void updateActiveContent(Pose newPose) {
        synchronized (mActiveContentLock) {
            if (mActiveContent != null) {
                mActiveContent.setNewPose(newPose);
            } else {
                Utils.throwError();
            }
        }
    }

    public void scaleActiveContent(float scale){
        synchronized (mActiveContentLock){
            if (mActiveContent != null){
                mActiveContent.scaleContent(scale);
            } else {
                Utils.throwError();
            }
        }
    }

    public void removeSavedActiveContent(){
        mSavedActiveContent = null;
    }

    public void removePendingActiveContent() {
        synchronized (mActiveContentLock) {
            if (mActiveContent != null) {
                Log.d(TAG, "removePendingActiveContent() called with: " + mActiveContent.getStatus());
                if (mActiveContent.getStatus() == VisualContent.RenderStatus.Rendered) {
                    mActiveContent.setStatus(VisualContent.RenderStatus.PendingRemove);
                } else if (mActiveContent.getStatus() == VisualContent.RenderStatus.PendingRender) {
                    //mActiveContent.setStatus(RenderStatus.None);
                    mActiveContent = null;
                }
            } else {
                Utils.throwError();
            }
        }
    }

    /**
     * Is called when renderer renders active content
     */
    public void setActiveContentAdded() {
        synchronized (mActiveContentLock) {
            if (mActiveContent != null) {
                mActiveContent.setStatus(VisualContent.RenderStatus.Rendered);
            } else {
                Utils.throwError();
            }
        }
    }

    public void setActiveContentFinishFitting() {
        synchronized (mActiveContentLock) {
            synchronized (mStaticContentLock) {
                if (mActiveContent.getStatus() == VisualContent.RenderStatus.Rendered) {
                    int index = mStaticContent.indexOf(mActiveContent);
                    if (index == -1) {
                        mStaticContent.add(mActiveContent);
                    } else {
                        mStaticContent.set(index, mActiveContent);
                    }
                    mActiveContent = null;
                } else {
                    Utils.throwError();
                }
            }
        }
    }

    public void setActiveContentRemoved() {
        synchronized (mActiveContentLock) {
            if (mActiveContent.getStatus() == VisualContent.RenderStatus.PendingRemove || mActiveContent.getStatus() == VisualContent.RenderStatus.None) {
                mActiveContent = null;
            } else {
                Utils.throwError();
            }
        }
    }

    public ActiveVisualContent getActiveContent() {
        synchronized (mActiveContentLock) {
            return mActiveContent;
        }
    }

    public boolean isActiveContent(){
        synchronized (mActiveContentLock){
            return mActiveContent != null;
        }
    }

    /**
     * Is called from Render thread. So renderer can decide to add active content
     *
     * @return
     */
    public boolean shouldActiveContentRenderOnScreen() {
        synchronized (mLocalizationLock) {
            synchronized (mActiveContentLock) {
                return mActiveContent != null && mActiveContent.getStatus() == VisualContent.RenderStatus.PendingRender && isLocalized();
            }
        }
    }

    /**
     * Is called from Render thread. So renderer can decide to remove rendered active content
     *
     * @return
     */
    public boolean shouldActiveContentRemoveFromScreen() {
        synchronized (mActiveContentLock) {
            return mActiveContent != null && mActiveContent.getStatus() == VisualContent.RenderStatus.PendingRemove;
        }
    }


    /******************************************
     * Static Content
     ******************************************/

    public void createStaticContent(final Collection<Content> collection) {
        synchronized (mLocalizationLock) {
            synchronized (mStaticContentLock) {
                if (isLocalized()) {
                    Log.d(TAG, "createStaticContent() isLocalized");
                    for (Content c : collection) {
                        addPendingStaticContent(c);
                    }

                } else {
                    Log.d(TAG, "createStaticContent() is not Localized");
                    for (Content c : collection) {
                        addSavedPendingStaticContent(new VisualContent(c));
                    }
                }
            }
        }
    }

    public void addPendingStaticContent(Content content){
        addPendingStaticContent(new VisualContent(content));
    }

    public void removePendingStaticContent(Content content) {
        synchronized (mStaticContentLock) {
            VisualContent vc = findVisualContentByContent(content);
            if (vc != null) {
                removePendingStaticContent(vc);
            }
        }
    }

    public void setStaticContentAdded(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            visualContent.setStatus(VisualContent.RenderStatus.Rendered);
            int index = mStaticContent.indexOf(visualContent);
            if (index != -1) {
                mStaticContent.set(index, visualContent);
            } else {
                Utils.throwError();

            }
        }
    }

    public void setStaticContentRemoved(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            int index = mStaticContent.indexOf(visualContent);
            if (index != -1) {
                mStaticContent.remove(visualContent);
            } else {
                Utils.throwError();
            }
        }
    }

    public Iterator<VisualContent> getStaticVisualContentToAdd() {
        synchronized (mLocalizationLock) {
            synchronized (mStaticContentLock) {
                if (isLocalized()) {
                    return filterStaticVisualContentList(VisualContent.RenderStatus.PendingRender);
                } else {
                    return Collections.emptyIterator();
                }
            }
        }
    }

    public Iterator<VisualContent> getStaticVisualContentToRemove() {
        synchronized (mStaticContentLock) {
            return filterStaticVisualContentList(VisualContent.RenderStatus.PendingRemove);
        }
    }

    public VisualContent findVisualContentByContent(Content content) {
        synchronized (mStaticContentLock) {
            for (VisualContent vc : mStaticContent) {
                if (vc.getContent().equals(content)) {
                    return vc;
                }
            }
            return null;
        }
    }

    public VisualContent findContentByObject3D(Object3D object) {
        synchronized (mStaticContentLock) {
            for (VisualContent vc : mStaticContent) {
                if (vc.getVisual().equals(object) && vc.getStatus() == VisualContent.RenderStatus.Rendered) {
                    return vc;
                }
            }
        }
        return null;
    }

    private Iterator<VisualContent> filterStaticVisualContentList(VisualContent.RenderStatus status) {
        List<VisualContent> res = new ArrayList<>();
        for (VisualContent vc : mStaticContent) {
            if (vc.getStatus() == status) {
                res.add(vc);
            }
        }
        return res.iterator();
    }


    private void addPendingStaticContent(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            visualContent.setStatus(VisualContent.RenderStatus.PendingRender);
            int index = mStaticContent.indexOf(visualContent);
            if (index == -1) {
                mStaticContent.add(visualContent);
            } else {
                mStaticContent.set(index, visualContent);
            }
        }
    }

    private void addSavedPendingStaticContent(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            visualContent.setStatus(VisualContent.RenderStatus.PendingRender);
            int index = mSavedStaticContent.indexOf(visualContent);
            if (index == -1) {
                mSavedStaticContent.add(visualContent);
            } else {
                mSavedStaticContent.set(index, visualContent);
            }
        }
    }

    private void removePendingStaticContent(VisualContent visualContent) {
        synchronized (mStaticContentLock) {
            visualContent.setStatus(VisualContent.RenderStatus.PendingRemove);
            int index = mStaticContent.indexOf(visualContent);
            if (index != -1) {
                mStaticContent.set(index, visualContent);
            }
        }
    }

    /******************************************
     * Selected Content
     ******************************************/

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

//    public void activeContentInHands(Pose newPose) {
//        synchronized (mActiveContentLock){
//            if (mActiveContent != null){
//                mActiveContent.setNewPose();
//            }
//        }
//    }
}
