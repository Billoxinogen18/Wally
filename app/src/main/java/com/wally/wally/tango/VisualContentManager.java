package com.wally.wally.tango;

import com.wally.wally.datacontroller.content.Content;

import org.rajawali3d.Object3D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shota on 4/25/16.
 */
public class VisualContentManager {
    private static final String TAG = VisualContentManager.class.getSimpleName();

    private List<VisualContent> staticContent = null;
    private ActiveVisualContent activeContent = null;
    private boolean mStaticContentShouldBeRendered = false;

    public VisualContentManager() {
        staticContent = new ArrayList<>();
    }

    public boolean isContentBeingAdded() {
        return activeContent != null;
    }

    public boolean getStaticContentShouldBeRendered() {
        return mStaticContentShouldBeRendered;
    }

    public void activeContentAddingFinished() {
        addStaticContent(activeContent);
        activeContent = null;
    }

    public boolean hasStaticContent() {
        return staticContent != null && staticContent.size() > 0;
    }

    public synchronized void addStaticContent(VisualContent visualContent) {
        staticContent.add(visualContent);
        mStaticContentShouldBeRendered = true;
    }

    public synchronized void removeStaticContent(VisualContent visualContent){
        staticContent.remove(visualContent);
        mStaticContentShouldBeRendered = true;
    }

    public synchronized List<VisualContent> getStaticContent() {
        return staticContent;
    }

    public synchronized ActiveVisualContent getActiveContent() {
        return activeContent;
    }

    public synchronized void setActiveContent(ActiveVisualContent activeContent) {
        this.activeContent = activeContent;
    }

    public void scaleActiveContent(float scaleFactor) {
        if(activeContent != null) {
            activeContent.getObject3D().setScale(activeContent.getObject3D().getScale().x * scaleFactor);
        }
    }

    /**
     * Search in static visual contents with object and get content object.
     *
     * @param object visual object3D
     * @return visual content
     */
    public VisualContent findContentByObject3D(Object3D object) {
        // TODO make with hashmap to get better performance
        for (VisualContent vc : staticContent) {
            if (vc.getObject3D().equals(object)) {
                return vc;
            }
        }
        return null;
    }


    public VisualContent findVisualContentByContent(Content content) {
        // TODO make with hashmap to get better performance
        for (VisualContent vc : staticContent) {
            if (vc.getContent().equals(content)) {
                return vc;
            }
        }
        return null;
    }
}
