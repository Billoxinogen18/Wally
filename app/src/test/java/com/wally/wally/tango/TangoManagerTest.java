package com.wally.wally.tango;


import com.google.atap.tangoservice.Tango;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.wally.wally.adf.AdfInfo;
import com.wally.wally.adf.AdfManager;
import com.wally.wally.renderer.WallyRenderer;
import com.wally.wally.ux.WallyTangoUx;

import org.junit.Before;

import static org.mockito.Mockito.mock;


/**
 * Created by shota on 5/24/16.
 */

public class TangoManagerTest {
    AdfInfo adf;
//    private TangoManager mTangoManager;
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
        adf = mock(AdfInfo.class);

    }

//
//    @Test
//    public void adfTest1(){
//        when(adf.isImported()).thenReturn(true);
//        when(adf.getUuid()).thenReturn("someUuid");
//        when(adfManager.hasAdf()).thenReturn(true).thenReturn(false);
//        when(adfManager.isAdfReady()).thenReturn(true);
//        when(adfManager.getAdf()).thenReturn(adf);
//        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, renderer, tangoUx, tangoFactory, adfManager, 200);
//        mTangoManager.onResume();
//        Utils.sleep(100);
//        //tangoUpdater.
//
//    }
//
//    @Test
//    public void adfTest2(){
//        when(adfManager.hasAdf()).thenReturn(false);
//        when(tangoFactory.getTango(any(Runnable.class))).thenReturn(tango);
//        mTangoManager.startTango(null);
//        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, renderer, tangoUx, tangoFactory, adfManager, 200);
//        mTangoManager.onResume();
//
//
//        assertThat(tango.getConfig(0).getBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE)
//                , is(true));
//    }
//
//    @Test
//    public void adfTest3(){
//        when(adfManager.hasAdf()).thenReturn(false);
//        mTangoManager = new TangoManager(tangoUpdater, pointCloudManager, renderer, tangoUx, tangoFactory, adfManager, 200);
//        mTangoManager.onResume();
//    }


}
