package com.wally.wally.controllers.main;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.BaseActivity;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.analytics.WallyAnalytics;
import com.wally.wally.components.LoadingFab;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.controllers.contentCreator.NewContentDialogFragment;
import com.wally.wally.controllers.map.MapsFragment;
import com.wally.wally.datacontroller.DataController;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.renderer.OnVisualContentSelectedListener;
import com.wally.wally.renderer.VisualContent;
import com.wally.wally.tip.TipView;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.SocialUserManager;

import org.rajawali3d.surface.RajawaliTextureView;

import java.util.Date;
import java.util.List;

public abstract class CameraARActivity extends BaseActivity implements
        OnVisualContentSelectedListener,
        NewContentDialogFragment.NewContentDialogListener,
        SelectedMenuView.OnSelectedMenuActionListener,
        MapsFragment.MapOpenCloseListener {

    private static final String TAG = CameraARActivity.class.getSimpleName();
    private static final int RC_SAVE_CONTENT = 22;

    private DataController mDataController;
    protected GoogleApiClient mGoogleApiClient;
    protected WallyAnalytics mAnalytics;
    protected TipView mTipView; //TODO getter maybe?
    private SocialUserManager mSocialUserManager;
    private long mLastSelectTime;
    private Content mSelectedContent; //TODO may be needed to remove
    private Content mContentToSave;
    private long mNewContentButtonLastClickTime;
    // Views
    private SelectedMenuView mSelectedMenuView;
    private RajawaliTextureView mRajawaliView;
    protected LoadingFab mNewContentButton;
    private View mMapButton;
    private View mProfileBar;
    private View mWaterMark;

    protected abstract void onDeleteContent(Content selectedContent);

    protected abstract void onSaveContent(Content selectedContent);

    // Called When content object is created by user
    @Override
    public abstract void onContentCreated(Content content, boolean isEditMode);

    /**
     * @return true if new content can be added
     */
    protected abstract boolean isNewContentCreationEnabled();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRajawaliView = (RajawaliTextureView) findViewById(R.id.rajawali_render_view);
        mSelectedMenuView = (SelectedMenuView) findViewById(R.id.selected_menu_view);
        mSelectedMenuView.setOnSelectedMenuActionListener(this);
        // Initialize managers
        mSocialUserManager = ((App) getApplicationContext()).getSocialUserManager(); //TODO get LoginManager from the Factory!
        mDataController = ((App) getApplicationContext()).getDataController();

        mNewContentButton = (LoadingFab) findViewById(R.id.new_post);
        mMapButton = findViewById(R.id.btn_map);
        mProfileBar = findViewById(R.id.profile_bar);
        mWaterMark = findViewById(R.id.watermark);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Plus.API)
                .addApi(LocationServices.API)
                .build();

        mAnalytics = WallyAnalytics.getInstance(this);
        mTipView = (TipView) findViewById(R.id.tip_view);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSocialUserManager.isLoggedIn()) {
            displayProfileBar(mSocialUserManager.getUser());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mSelectedContent", mSelectedContent);
        outState.putSerializable("mContentToSave", mContentToSave);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedContent = (Content) savedInstanceState.getSerializable("mSelectedContent");
        mContentToSave = (Content) savedInstanceState.getSerializable("mContentToSave");
        onContentSelected(mSelectedContent);
    }

    private void onContentSelected(final Content content) {
        mSelectedContent = content;
        if (App.getInstance().getSocialUserManager().isLoggedIn()) {
            runOnUiThread(new Runnable() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void run() {
                    mSelectedMenuView.setVisibility(content == null ? View.GONE : View.VISIBLE);
                    mSelectedMenuView.setContent(content, mGoogleApiClient);
                    mLastSelectTime = System.currentTimeMillis();

                    mSelectedMenuView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Hide iff user didn't click after
                            if (mLastSelectTime + 3000 <= System.currentTimeMillis()) {
                                mSelectedMenuView.setVisibility(View.GONE);
                                mSelectedContent = null;
                            }
                        }
                    }, 3000);
                }
            });
        }
    }

    @Override
    public void onVisualContentSelected(VisualContent visualContent) {
        if (visualContent != null) {
            mAnalytics.onButtonClick("Content_Selected");
        } else {
            mAnalytics.onButtonClick("Click_On_Camera");
        }

        Content content = null;
        if (visualContent != null) {
            content = visualContent.getContent();
        }
        onContentSelected(content);
    }


    public void onNewContentClick(View v) {
        if (!isNewContentCreationEnabled()) {
            return;
        }
        mAnalytics.onButtonClick("New_Content");
        if (SystemClock.elapsedRealtime() - mNewContentButtonLastClickTime < 1000) {
            return;
        }
        mNewContentButtonLastClickTime = SystemClock.elapsedRealtime();

        NewContentDialogFragment.newInstance()
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    public void onBtnMapClick(View v) {
        mAnalytics.onButtonClick("Map");
        openMapFragment(null);
    }

    public void onShowProfileClick(View v) {
        onProfileClick(mSocialUserManager.getUser(), true);
    }


    public void onEditSelectedContentClick(Content content) {
        mAnalytics.onButtonClick("Edit_Content");
        if (content == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        Log.d(TAG, "onEditSelectedContentClick() called with: " + "content = [" + content + "]");
        NewContentDialogFragment.newInstance(content)
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    public void onDeleteSelectedContentClick(Content content) {
        mAnalytics.onButtonClick("Delete_Content");
        if (content == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        //delete content on the server
        mDataController.delete(content);
        onDeleteContent(content);
    }

    public void onProfileClick(SocialUser user, boolean type) {
        if (type) {
            mAnalytics.onButtonClick("My_Profile");
        } else {
            mAnalytics.onButtonClick("Some_Profile");
        }
        openMapFragment(user);
    }

    @CallSuper
    @Override
    protected void onLocationPermissionGranted(int locationRequestCode) {
        if (locationRequestCode == RC_SAVE_CONTENT) {
            saveActiveContent(mContentToSave);
        }
    }

    protected void saveActiveContent(final Content content) {
        mContentToSave = content;
        if (!Utils.checkHasLocationPermission(this)) {
            requestLocationPermission(RC_SAVE_CONTENT);
            return;
        }
        Utils.getLocation(mGoogleApiClient, new Utils.Callback<SerializableLatLng>() {
            @Override
            public void onResult(SerializableLatLng result) {
                Log.d(TAG, "onResult() called with: " + "result = [" + result + "]");
                content.withLocation(result);
                if (content.getCreationDate() == null) {
                    content.withCreationDate(new Date(System.currentTimeMillis()));
                }
                onSaveContent(content);
                mDataController.save(content);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "getNewLocation onError: " + e);
            }
        });
    }


    @SuppressWarnings("ConstantConditions")
    private void displayProfileBar(SocialUser user) {
        UserInfoView infoView = (UserInfoView) findViewById(R.id.profile_bar);
        infoView.setVisibility(View.VISIBLE);
        infoView.setUser(user);
    }

    public void openMapFragment(SocialUser user) {
        MapsFragment mf = MapsFragment.newInstance(user);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, mf);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void hideGUI(boolean hide) {
        if (hide) {
            mRajawaliView.setFrameRate(10);
            mNewContentButton.setVisibility(View.GONE);
            mMapButton.setVisibility(View.GONE);
            mProfileBar.setVisibility(View.GONE);
            mWaterMark.setVisibility(View.GONE);
        } else {
            mRajawaliView.setFrameRate(30);
            mNewContentButton.setVisibility(isNewContentCreationEnabled() ? View.VISIBLE : View.GONE);
            mMapButton.setVisibility(View.VISIBLE);
            mProfileBar.setVisibility(View.VISIBLE);
            mWaterMark.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapClose() {
        if (!isMapsFragmentAttached()) {
            hideGUI(false);
        }
    }

    @Override
    public void onMapOpen() {
        hideGUI(true);
    }

    private boolean isMapsFragmentAttached() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList == null) {
            return false;
        } else {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof MapsFragment && fragment.isInLayout()) {
                    return true;
                }
            }
        }
        return false;
    }
}
