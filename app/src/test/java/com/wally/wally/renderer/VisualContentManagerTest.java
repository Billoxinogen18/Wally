package com.wally.wally.renderer;

import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.renderer.VisualContent;
import com.wally.wally.renderer.VisualContentManager;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by shota on 5/26/16.
 */
public class VisualContentManagerTest {
    private VisualContentManager mVisualContentManager;
    private Pose mPose;
    private Content mContent;
    private Collection<Content> mContents;



    @Before
    public void init(){
        mVisualContentManager = new VisualContentManager();
        mPose = new Pose(new Vector3(1,1,1), new Quaternion(2,2,2,2));
        mContent = mock(Content.class);
        mContents = getCollection(5);
    }

    /******************************************
     * Active Content
     ******************************************/


    @Test
    public void activeContentTest1(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.visualContentRestoreAndShow();
        assertThat(mVisualContentManager.getActiveContent().getContent(), is(mContent));
        assertThat(mVisualContentManager.getActiveContent().getStatus(), is(VisualContent.RenderStatus.PendingRender));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(true));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
    }

//§

    @Test
    public void activeContentTest3(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        assertThat(mVisualContentManager.getActiveContent().getStatus(),is(VisualContent.RenderStatus.Rendered));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
    }

    @Test
    public void activeContentTest4(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.removePendingActiveContent();
        assertThat(mVisualContentManager.getActiveContent().getStatus(),is(VisualContent.RenderStatus.PendingRemove));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(true));
    }

    @Test
    public void activeContentTest5(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        VisualContent con = mVisualContentManager.getActiveContent();
        mVisualContentManager.setActiveContentFinishFitting();
        assertThat(mVisualContentManager.getActiveContent(),is(IsNull.nullValue()));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
        assertThat(mVisualContentManager.findVisualContentByContent(mContent), is(con));
        assertThat(mVisualContentManager.findVisualContentByContent(mContent).getStatus(),is(VisualContent.RenderStatus.Rendered));
    }

    @Test
    public void activeContentTest51(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.removePendingActiveContent();
        //mVisualContentManager.setActiveContentRemoved();
        assertThat(mVisualContentManager.getActiveContent(),is(IsNull.nullValue()));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest6(){
        mVisualContentManager.removePendingActiveContent();
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest7(){
        mVisualContentManager.setActiveContentAdded();
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest8(){
        mVisualContentManager.updateActiveContent(null);
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest9(){
        mVisualContentManager.scaleActiveContent(1);
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest10(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentFinishFitting();
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest11(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentRemoved();
    }

    @Test
    public void activeContentTest12(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.removePendingActiveContent();
        mVisualContentManager.setActiveContentRemoved();
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
    }

    @Test(expected = RuntimeException.class)
    public void activeContentTest13(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.removePendingActiveContent();
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
    }


    /******************************************
     * Static Content
     ******************************************/

    private Content getRandomContent(int n){
        return new Content(""+n);
    }

    private Collection<Content> getCollection(int n){
        Collection<Content> res = new ArrayList<>();
        for (int i=0; i<n; i++){
            res.add(getRandomContent(i));
        }
        return res;
    }

    private <T> ArrayList<T> getArrayListFromIterator(Iterator<T> a){
        ArrayList<T> res = new ArrayList<a>();
        while(a.hasNext()){
            res.add(a.next());
        }
        return res;
    }

    private boolean iteratorEquals(Iterator<VisualContent> a, Iterator<Content> b){
        ArrayList<VisualContent> l1 = getArrayListFromIterator(a);
        ArrayList<Content> l2 = getArrayListFromIterator(b);
        int index = 0;
        while (l1.size() > 0){
            VisualContent o = l1.get(index);
            if (l2.contains(o.getContent())){
                l2.remove(o.getContent());
                l1.remove(o);
            } else {
                return false;
            }
        }
        return l2.size() == 0;
    }

    private int iteratorSize(Iterator a){
        return getArrayListFromIterator(a).size();
    }

    @Test
    public void staticContentTest1(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        boolean eq1 = iteratorEquals(mVisualContentManager.getStaticVisualContentToAdd(), mContents.iterator());
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));
        assertThat(eq1, is(true));
    }

    @Test
    public void staticContentTest2(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();

        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));

    }

    @Test
    public void staticContentTest3(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        for (Content vc: mContents) {
            mVisualContentManager.removePendingStaticContent(vc);
        }

        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        boolean eq2 = iteratorEquals(mVisualContentManager.getStaticVisualContentToRemove(), mContents.iterator());
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(iteratorSize(mContents.iterator())));
        assertThat(eq2, is(true));
    }


    @Test
    public void staticContentTest4() {
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        for (Content vc : mContents) {
            mVisualContentManager.removePendingStaticContent(vc);
        }
        removeAllPendingStaticContent();
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));

    }


    @Test(expected = RuntimeException.class)
    public void staticContentTest5(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.setStaticContentAdded(new VisualContent(mContent));
    }

    @Test(expected = RuntimeException.class)
    public void staticContentTest6(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.removePendingStaticContent(mContent);
    }

    @Test(expected = RuntimeException.class)
    public void staticContentTest7(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.setStaticContentRemoved(new VisualContent(mContent));
    }

    @Test(expected = RuntimeException.class)
    public void staticContentTest8(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        mVisualContentManager.setStaticContentRemoved(new VisualContent(mContents.iterator().next()));
    }

    @Test
    public void staticContentTest9(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        for (Content c: mContents) {
            mVisualContentManager.removePendingStaticContent(c);
        }
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        boolean eq2 = iteratorEquals(mVisualContentManager.getStaticVisualContentToRemove(), mContents.iterator());
        assertThat(eq2, is(true));
    }


    private void addAllPendingStaticContent(){
        Iterator<VisualContent> it = mVisualContentManager.getStaticVisualContentToAdd();
        while (it.hasNext()) {
            VisualContent vc = it.next();
            mVisualContentManager.setStaticContentAdded(vc);
        }
    }

    private void removeAllPendingStaticContent(){
        Iterator<VisualContent> it2 = mVisualContentManager.getStaticVisualContentToRemove();
        while(it2.hasNext()){
            VisualContent vc = it2.next();
            mVisualContentManager.setStaticContentRemoved(vc);
        }
    }

    /******************************************
     * Localization
     ******************************************/

    @Test
    public void localizationTest1(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        mVisualContentManager.visualContentSaveAndClear();
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));
    }

    @Test
    public void localizationTest2(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        mVisualContentManager.visualContentSaveAndClear();

        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        boolean eq1 = iteratorEquals(mVisualContentManager.getStaticVisualContentToRemove(), mContents.iterator());
        assertThat(eq1, is(true));
    }

    @Test
    public void localizationTest3(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        mVisualContentManager.visualContentSaveAndClear();
        mVisualContentManager.visualContentRestoreAndShow();

        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToAdd()), is(0));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));
    }

    @Test
    public void localizationTest4(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        mVisualContentManager.visualContentSaveAndClear();
        removeAllPendingStaticContent();
        addAllPendingStaticContent();
        mVisualContentManager.visualContentRestoreAndShow();

        boolean eq = iteratorEquals(mVisualContentManager.getStaticVisualContentToAdd(), mContents.iterator());
        assertThat(eq, is(true));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));
    }

    @Test
    public void localizationTest5(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.createStaticContent(mContents);
        addAllPendingStaticContent();
        mVisualContentManager.visualContentSaveAndClear();
        removeAllPendingStaticContent();
        addAllPendingStaticContent();
        mVisualContentManager.visualContentRestoreAndShow();

        boolean eq = iteratorEquals(mVisualContentManager.getStaticVisualContentToAdd(), mContents.iterator());
        assertThat(eq, is(true));
        assertThat(iteratorSize(mVisualContentManager.getStaticVisualContentToRemove()), is(0));
    }



    @Test
    public void localizationTest6(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
    }


    @Test
    public void localizationTest7(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.visualContentSaveAndClear();
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(true));
    }

    @Test
    public void localizationTest8(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.visualContentSaveAndClear();
        mVisualContentManager.visualContentRestoreAndShow();
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
    }



    @Test
    public void localizationTest9(){
        mVisualContentManager.visualContentRestoreAndShow();
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.setActiveContentAdded();
        mVisualContentManager.visualContentSaveAndClear();
        mVisualContentManager.setActiveContentRemoved();
        mVisualContentManager.visualContentRestoreAndShow();
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(true));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
    }

//    @Test
//    public void getLocalizationNewContentTest(){
//        ArrayList<VisualContent> v = getVCList(5);
//        ArrayList<VisualContent> v2 = getVCList(5);
//        ArrayList<VisualContent> res = mVisualContentManager.getLocalizationNewContent(v,v2);
//        assertThat(v.size(), is(res.size()));
//        for (int i=0; i<v.size(); i++){
//            assertThat(res.contains(v.get(i)), is(true));
//        }
//    }
//
//    private ArrayList<VisualContent> getVCList(int n){
//        ArrayList<VisualContent> res = new ArrayList<>();
//        for (int i=0; i<n; i++){
//            res.add(getVisualContent(i));
//        }
//        return res;
//    }
//
//    private VisualContent getVisualContent(int id){
//        VisualContent v = new VisualContent(new Content(""+id));
//        v.setStatus(VisualContent.RenderStatus.Rendered);
//        return v;
//    }

}
