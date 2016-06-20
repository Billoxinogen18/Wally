package com.wally.wally;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.tango.WallyRenderer;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UtilsTest {
    @Test
    public void test1(){
        assertTrue(true);


        Context x = mock(Context.class);
        WindowManager m = mock(WindowManager.class);
        Display d = mock(Display.class);
        when(m.getDefaultDisplay()).thenReturn(d);
        when(x.getSystemService(Context.WINDOW_SERVICE)).thenReturn(m);
        new WallyRenderer(x,null,null);

    }

}
