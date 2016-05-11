package com.wally.wally.tango;

import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shota on 4/29/16.
 * Class controls Rajawali scene. It stores every content rendered on the scene.
 */
public class VisualContentManager {
    private static final String TAG = VisualContentManager.class.getSimpleName();

    private List<VisualContent> mToBeRenderedOnScene;
    private List<VisualContent> mAlreadyRenderedOnScene;
    private ActiveVisualContent mActiveContent;
    private boolean mIsActiveContentRenderedOnScreen;
    private VisualContent mSelectedContent;
    private boolean mBorderOnScreen;

    public VisualContentManager() {
        mToBeRenderedOnScene = new ArrayList<>();
        mAlreadyRenderedOnScene = new ArrayList<>();
        mActiveContent = null;
        mIsActiveContentRenderedOnScreen = false;
    }

    // ------------------------   active content ------------------------------

    //TODO do we need synchronization?
    public synchronized void setActiveContentToBeRenderedOnScreen(ActiveVisualContent activeContent) {
        this.mActiveContent = activeContent;
        mIsActiveContentRenderedOnScreen = false;
    }

    public synchronized void activeContentAlreadyRenderedOnScreen() {
        mIsActiveContentRenderedOnScreen = true;
    }

    public synchronized boolean isActiveContentRenderedOnScreen() {
        return mActiveContent != null;
    }

    public synchronized void removeActiveContent() {
        mActiveContent = null;
        mIsActiveContentRenderedOnScreen = false;
    }

    public synchronized boolean shouldActiveContentREnderOnScreen() {
        return !mIsActiveContentRenderedOnScreen && mActiveContent != null;
    }

    public synchronized void activeContentAddingFinished() {
        addStaticContentToBeRenderedOnScreen(mActiveContent); //TODO check addToBeRenderedOnScreen or addAlreadyRenderedOnScreen
//        mActiveContent = null;
    }

    public synchronized ActiveVisualContent getActiveContent() {
        return mActiveContent;
    }

    // ------------------------   static content ------------------------------

    public synchronized void addStaticContentToBeRenderedOnScreen(VisualContent visualContent) {
        if (!mToBeRenderedOnScene.contains(visualContent)) {
            mToBeRenderedOnScene.add(visualContent);
        }
    }

    public synchronized void removeStaticContentToBeRenderedOnScreen(VisualContent visualContent) {
        mToBeRenderedOnScene.remove(visualContent);
    }

    public synchronized List<VisualContent> getStaticContentToBeRenderedOnScreen() {
        return mToBeRenderedOnScene;
    }

    public synchronized boolean hasStaticContentToBeRendered() {
        return mToBeRenderedOnScene != null && mToBeRenderedOnScene.size() > 0;
    }

    public synchronized void addStaticContentAlreadyRenderedOnScreen(VisualContent visualContent) {
        if (!mAlreadyRenderedOnScene.contains(visualContent)) {
            mAlreadyRenderedOnScene.add(visualContent);
        }
        mToBeRenderedOnScene.remove(visualContent);

    }

    public synchronized void removeStaticContentAlreadyRenderedOnScreen(VisualContent visualContent) {
        mAlreadyRenderedOnScene.remove(visualContent);
    }

    public synchronized boolean isContentOnScreen(VisualContent visualContent){
        return mAlreadyRenderedOnScene.contains(visualContent);
    }

    // ------------------------   selected content ------------------------------

    public VisualContent getSelectedContent() {
        return mSelectedContent;
    }

    public synchronized void setSelectedContent(VisualContent visualContent) {
        mSelectedContent = visualContent;
    }

    public synchronized boolean isSelectedContent(VisualContent visualContent) {
        return visualContent == mSelectedContent;
    }

    // ------------------------   border content ------------------------------


    public synchronized boolean isBorderOnScreen() {
        return mBorderOnScreen;
    }

    public synchronized void setBorderOnScreen(boolean borderOnScreen) {
        this.mBorderOnScreen = borderOnScreen;
    }

    /**
     * Search in static visual contents with object and get content object.
     *
     * @param object visual object3D
     * @return visual content
     */
    public synchronized VisualContent findContentByObject3D(Object3D object) {
        // TODO make with hashmap to get better performance
        for (VisualContent vc : mAlreadyRenderedOnScene) {
            if (vc.getVisual().equals(object)) {
                return vc;
            }
        }
        return null;
    }

    public synchronized VisualContent findVisualContentByContent(Content content) {
        // TODO make with hashmap to get better performance
        for (VisualContent vc : mAlreadyRenderedOnScene) {
            if (vc.getContent().equals(content)) {
                return vc;
            }
        }
        return null;
    }

//
//    public void addActiveContentOnScene(Content content, RajawaliScene scene){
//        mActiveContent = new ActiveVisualContent(content);
//        scene.addChild(mActiveContent);
//        //activeContent.animate(scene);
//    }
//
//    public void removeActiveContentFromScene(RajawaliScene scene){
//        scene.removeChild(mActiveContent);
//        mActiveContent = null;
//    }
//
//    public void saveActiveContent(){
//        mStaticContent.add(mActiveContent);
//
//    }
//

//

//

//

//

//

//

//

//

//
//

//
//    public void deselectAll() {
//        //TODO do better selection deselection algorithm
//        for (VisualContent vc : mStaticContent) {
//
//        }
//    }
}

