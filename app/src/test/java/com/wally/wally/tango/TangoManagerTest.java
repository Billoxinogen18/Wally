package com.wally.wally.tango;



import android.content.Context;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.rajawali3d.surface.RajawaliSurfaceView;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


/**
 * Created by shota on 5/24/16.
 */

@SmallTest
public class TangoManagerTest {
    private TangoManager mTangoManager;


    private Context context;
    private RajawaliSurfaceView rajawaliSurfaceView;
    private TangoUxLayout tangoUxLayout;
    private WallyRenderer renderer;
    private VisualContentManager visualContentManager;
    TangoPointCloudManager pointCloudManager;
    private TangoUx tangoUx;
    private Tango tango;
    private String uid;


    @Before
    public void init(){
        pointCloudManager = mock(TangoPointCloudManager.class);
        context = mock(Context.class);
        rajawaliSurfaceView = mock(RajawaliSurfaceView.class);
        tangoUxLayout = mock(TangoUxLayout.class);
        renderer = mock(WallyRenderer.class);
        visualContentManager = mock(VisualContentManager.class);
        tangoUx = mock(TangoUx.class);
        tango = TangoMock.getTango();

    }

    @Test
    public void findPlaneInMiddleTest1(){
        when(pointCloudManager.getLatestXyzIj()).thenReturn(null);
        mTangoManager = new TangoManager(context, rajawaliSurfaceView, tangoUxLayout,
                pointCloudManager, visualContentManager, renderer, tangoUx, tango, uid);
        assertNull(mTangoManager.findPlaneInMiddle());
;    }



}
