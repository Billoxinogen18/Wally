package com.wally.wally.renderer;

import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.renderer.VisualContent;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by shota on 5/24/16.
 */
public class VisualContentTest {
    private VisualContent mVisualContent;

    @Test
    public void refreshVisualScaleTest1(){
        mVisualContent = new VisualContent(new Content());
        mVisualContent.refreshVisualScale();
    }

    @Test
    public void refreshVisualScaleTest2(){
        Content content = Mockito.mock(Content.class);
        Mockito.when(content.getTangoData()).thenReturn(new TangoData());
        mVisualContent = new VisualContent(new Content());
        mVisualContent.refreshVisualScale();
    }
//
//    @Test
//    public void refreshVisualScaleTest3(){
//        Content content = Mockito.mock(Content.class);
//        Mockito.when(content.getTangoData()).thenReturn(Mockito.mock(TangoData.class));
//        Mockito.when(content.getTangoData().getScale()).thenReturn(3D);
//        mVisualContent = new VisualContent(new Content());
//        mVisualContent.refreshVisualScale();
//        assertThat(mVisualContent.getVisual().getScale(), is(new Vector3(3,3,3)));
//    }
}
