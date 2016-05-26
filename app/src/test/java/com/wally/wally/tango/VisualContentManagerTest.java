package com.wally.wally.tango;

import org.junit.*;

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

    @Test
    public void activeContentTest1(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
        assertThat(mVisualContentManager.getActiveContent(), is(a));
    }

    @Test
    public void activeContentTest2(){
        ActiveVisualContent a = mock(ActiveVisualContent.class);
        mVisualContentManager.setActiveContentToBeRenderedOnScreen(a);
        assertThat(mVisualContentManager.isActiveContentRenderedOnScreen(), is(false));
    }


}
