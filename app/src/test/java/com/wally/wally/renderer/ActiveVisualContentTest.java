package com.wally.wally.renderer;

import com.projecttango.rajawali.Pose;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;

import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by shota on 5/25/16.
 *
 */
public class ActiveVisualContentTest {
    private ActiveVisualContent mActiveVisualContent;



    @Test
    public void shouldAnimateTest1(){
        mActiveVisualContent = new ActiveVisualContent(new Content());
        assertThat(mActiveVisualContent.shouldAnimate(), is(false));
    }

    @Test
    public void shouldAnimateTest2(){
        mActiveVisualContent = new ActiveVisualContent(new Content());
        mActiveVisualContent.setNewPose(mock(Pose.class));
        assertThat(mActiveVisualContent.shouldAnimate(), is(true));
    }

    @Test
    public void animateTest1(){
        mActiveVisualContent = new ActiveVisualContent(new Content());
        Pose p = mock(Pose.class);
        when(p.getPosition()).thenReturn(new Vector3());
        mActiveVisualContent.setNewPose(p);
        mActiveVisualContent.animate(mock(RajawaliScene.class));
        assertThat(mActiveVisualContent.shouldAnimate(), is(true));
    }

    @Test
    public void scaleContentByFactorTest1(){
        mActiveVisualContent = new ActiveVisualContent(new Content());
        mActiveVisualContent.scaleContent(5);
    }

    @Test
    public void scaleContentByFactorTest2(){
        Content c = mock(Content.class);
        when(c.getTangoData()).thenReturn(new TangoData());
        mActiveVisualContent = new ActiveVisualContent(c);
        mActiveVisualContent.scaleContent(5);
    }

    @Test
    public void scaleContentByFactorTest3(){
        Content c = mock(Content.class);
        TangoData td = new TangoData();
        td.withScale(3d);
        when(c.getTangoData()).thenReturn(td);
        mActiveVisualContent = new ActiveVisualContent(c);
        mActiveVisualContent.scaleContent(5);
        assertThat(mActiveVisualContent.getContent().getTangoData().getScale(), is(15D));
    }
}
