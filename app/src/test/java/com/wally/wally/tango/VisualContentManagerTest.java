package com.wally.wally.tango;

import com.projecttango.rajawali.ContentPlane;
import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;

import org.hamcrest.core.IsNull;
import org.junit.*;
import org.mockito.Mock;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * Created by shota on 5/26/16.
 */
public class VisualContentManagerTest {
    private VisualContentManager mVisualContentManager;
    private Pose mPose;
    private Content mContent;



    @Before
    public void init(){
        mVisualContentManager = new VisualContentManager();
        mPose = new Pose(new Vector3(1,1,1), new Quaternion(2,2,2,2));
        mContent = mock(Content.class);
    }

    @Test
    public void activeContentTest1(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        assertThat(mVisualContentManager.getActiveContent().getContent(), is(mContent));
        assertThat(mVisualContentManager.getActiveContent().getStatus(), is(VisualContent.RenderStatus.PendingRender));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(true));
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
    }

    @Test
    public void activeContentTest2(){
        mVisualContentManager.addPendingActiveContent(mPose, mContent);
        mVisualContentManager.removePendingActiveContent();
        assertThat(mVisualContentManager.shouldActiveContentRemoveFromScreen(), is(false));
        assertThat(mVisualContentManager.getActiveContent().getStatus(), is(VisualContent.RenderStatus.None));
        assertThat(mVisualContentManager.shouldActiveContentRenderOnScreen(), is(false));
    }

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
        mVisualContentManager.setActiveContentRemoved();
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



//    @Test
//    public void staticContentTest1(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().contains(a), is(true));
//    }
//
//    @Test
//    public void staticContentTest2(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
//    }
//
//    @Test
//    public void staticContentTest3(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(a);
//        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(false));
//    }
//
//    @Test
//    public void staticContentTest4(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        ActiveVisualContent b = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
//        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
//    }
//
//    @Test
//    public void staticContentTest5(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
//        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().size(), is(1));
//    }
//
//    @Test
//    public void staticContentTest6(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        ActiveVisualContent b = mock(ActiveVisualContent.class);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
//        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
//        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().size(), is(2));
//    }
//
//
//    @Test
//    public void findContentByObject3DTest1(){
//        VisualContent b = mock(VisualContent.class);
//        ContentPlane cp = mock(ContentPlane.class);
//        when(b.getVisual()).thenReturn(cp);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
//        assertThat(mVisualContentManager.findContentByObject3D(cp), is(IsNull.nullValue()));
//        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
//        assertThat(mVisualContentManager.findContentByObject3D(cp), is(b));
//    }
//
//    @Test
//    public void findContentByObject3DTest2(){
//        ContentPlane cp = mock(ContentPlane.class);
//        assertThat(mVisualContentManager.findContentByObject3D(cp), is(IsNull.nullValue()));
//    }
//
//
//    @Test
//    public void findVisualContentByContentTest1(){
//        VisualContent b = mock(VisualContent.class);
//        Content c = mock(Content.class);
//        when(b.getContent()).thenReturn(c);
//        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
//        assertThat(mVisualContentManager.findVisualContentByContent(c), is(IsNull.nullValue()));
//        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
//        assertThat(mVisualContentManager.findVisualContentByContent(c), is(b));
//    }
//
//    @Test
//    public void findVisualContentByContentTest2(){
//        Content c = mock(Content.class);
//        assertThat(mVisualContentManager.findVisualContentByContent(c), is(IsNull.nullValue()));
//    }

}
