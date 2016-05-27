package com.wally.wally.tango;

import com.projecttango.rajawali.ContentPlane;
import com.wally.wally.datacontroller.content.Content;

import org.hamcrest.core.IsNull;
import org.junit.*;
import org.rajawali3d.Object3D;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

/**
 * Created by shota on 5/26/16.
 */
public class VisualContentManagerTest {
    private VisualContentManager mVisualContentManager;

    @Before
    public void init(){
        mVisualContentManager = new VisualContentManager();
    }

//    @Test
//    public void activeContentTest1(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        assertThat(mVisualContentManager.getActiveContent(), is(a));
//    }
//
//    @Test
//    public void activeContentTest2(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        assertThat(mVisualContentManager.shouldActiveContentREnderOnScreen(), is(true));
//    }
//
//    @Test
//    public void activeContentTest3(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        mVisualContentManager.activeContentAlreadyRenderedOnScreen();
//        assertThat(mVisualContentManager.shouldActiveContentREnderOnScreen(), is(false));
//    }
//
//    @Test
//    public void activeContentTest4(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        mVisualContentManager.activeContentAlreadyRenderedOnScreen();
//        assertThat(mVisualContentManager.getActiveContent(), is(a));
//    }
//
//    @Test
//    public void activeContentTest5(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        mVisualContentManager.removeActiveContent();
//        assertThat(mVisualContentManager.getActiveContent(), is(IsNull.nullValue()));
//    }
//
//    @Test
//    public void activeContentTest6(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        mVisualContentManager.removeActiveContent();
//        assertThat(mVisualContentManager.shouldActiveContentREnderOnScreen(), is(false));
//    }
//
//
//    @Test
//    public void activeContentTest9(){
//        ActiveVisualContent a = mock(ActiveVisualContent.class);
//        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
//        mVisualContentManager.activeContentAddingFinished();
//        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().contains(a), is(true));
//    }


    @Test
    public void staticContentTest1(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().contains(a), is(true));
    }

    @Test
    public void staticContentTest2(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
    }

    @Test
    public void staticContentTest3(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(a);
        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(false));
    }

    @Test
    public void staticContentTest4(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        ActiveVisualContent b = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
    }

    @Test
    public void staticContentTest5(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().size(), is(1));
    }

    @Test
    public void staticContentTest6(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        ActiveVisualContent b = mock(ActiveVisualContent.class);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(a);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
        assertThat(mVisualContentManager.isStaticContentToBeRendered(), is(true));
        assertThat(mVisualContentManager.getStaticContentToBeRenderedOnScreen().size(), is(2));
    }


    @Test
    public void findContentByObject3DTest1(){
        VisualContent b = mock(VisualContent.class);
        ContentPlane cp = mock(ContentPlane.class);
        when(b.getVisual()).thenReturn(cp);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
        assertThat(mVisualContentManager.findContentByObject3D(cp), is(IsNull.nullValue()));
        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
        assertThat(mVisualContentManager.findContentByObject3D(cp), is(b));
    }

    @Test
    public void findContentByObject3DTest2(){
        ContentPlane cp = mock(ContentPlane.class);
        assertThat(mVisualContentManager.findContentByObject3D(cp), is(IsNull.nullValue()));
    }


    @Test
    public void findVisualContentByContentTest1(){
        VisualContent b = mock(VisualContent.class);
        Content c = mock(Content.class);
        when(b.getContent()).thenReturn(c);
        mVisualContentManager.addStaticContentToBeRenderedOnScreen(b);
        assertThat(mVisualContentManager.findVisualContentByContent(c), is(IsNull.nullValue()));
        mVisualContentManager.addStaticContentAlreadyRenderedOnScreen(b);
        assertThat(mVisualContentManager.findVisualContentByContent(c), is(b));
    }

    @Test
    public void findVisualContentByContentTest2(){
        Content c = mock(Content.class);
        assertThat(mVisualContentManager.findVisualContentByContent(c), is(IsNull.nullValue()));
    }

}
