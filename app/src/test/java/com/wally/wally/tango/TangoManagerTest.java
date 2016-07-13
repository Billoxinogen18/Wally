package com.wally.wally.tango;



import android.content.Context;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.Utils;
import com.wally.wally.adfCreator.AdfInfo;
import com.wally.wally.adfCreator.AdfManager;
import com.wally.wally.components.WallyTangoUx;

import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.surface.RajawaliSurfaceView;

import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.*;


/**
 * Created by shota on 5/24/16.
 */

@SmallTest
public class TangoManagerTest {
    private TangoManager mTangoManager;


    private WallyRenderer renderer;
    private TangoPointCloudManager pointCloudManager;
    private TangoUpdater tangoUpdater;
    private WallyTangoUx tangoUx;
    private TangoFactory tangoFactory;
    private AdfManager adfManager;
    private Tango tango;
    private String uid;


    @Before
    public void init(){
        pointCloudManager = mock(TangoPointCloudManager.class);
        renderer = mock(WallyRenderer.class);
        tangoUx = mock(WallyTangoUx.class);
        tango = TangoMock.getTango();
        tangoUpdater = mock(TangoUpdater.class);
        tangoFactory = mock(TangoFactory.class);
        adfManager = mock(AdfManager.class);
    }

    @Test
    public void findPlaneInMiddleTest1(){
//        when(pointCloudManager.getLatestXyzIj()).thenReturn(null);
//        mTangoManager = new TangoManager(context, rajawaliSurfaceView, tangoUxLayout,
//                pointCloudManager, visualContentManager, renderer, tangoUx, tango, uid);
//        assertNull(mTangoManager.findPlaneInMiddle());
;    }


    @Test
    public void adfTest1(){
        AdfInfo adf = new AdfInfo();
        when(adf.isImported()).thenReturn(true);
        when(adf.getUuid()).thenReturn("someUuid");
        when(adfManager.hasAdf()).thenReturn(true);
        when(adfManager.isAdfReady()).thenReturn(true);
        when(adfManager.getAdf()).thenReturn(adf);
        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, renderer, tangoUx, tangoFactory, adfManager, 200);
        mTangoManager.onResume();
        Utils.sleep(100);
        //tangoUpdater.

    }


}
