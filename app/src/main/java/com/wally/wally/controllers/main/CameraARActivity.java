package com.wally.wally.controllers.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.wally.wally.App;
import com.wally.wally.BaseActivity;
import com.wally.wally.BuildConfig;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.LoadingFab;
import com.wally.wally.components.PersistentDialogFragment;
import com.wally.wally.components.UserInfoView;
import com.wally.wally.controllers.PuzzleAnswerDialogFragment;
import com.wally.wally.controllers.contentCreator.NewContentDialogFragment;
import com.wally.wally.controllers.map.MapsFragment;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.Puzzle;
import com.wally.wally.objects.content.SerializableLatLng;
import com.wally.wally.renderer.OnVisualContentSelectedListener;
import com.wally.wally.renderer.VisualContent;
import com.wally.wally.tip.TipView;
import com.wally.wally.userManager.SocialUser;
import com.wally.wally.userManager.SocialUserManager;

import org.rajawali3d.surface.RajawaliSurfaceView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class CameraARActivity extends BaseActivity implements
        OnVisualContentSelectedListener,
        NewContentDialogFragment.NewContentDialogListener,
        SelectedMenuView.OnSelectedMenuActionListener,
        MapsFragment.MapOpenCloseListener,
        PersistentDialogFragment.PersistentDialogListener,
        PuzzleAnswerDialogFragment.PuzzleAnswerListener {

    private static final String TAG = CameraARActivity.class.getSimpleName();
    private static final int RC_SAVE_CONTENT = 22;
    private static final int RC_CHOOSE_PUZZLE_OR_NOTE = 921;

    protected GoogleApiClient mGoogleApiClient;

    private SocialUserManager mSocialUserManager;
    private long mLastSelectTime;
    private Content mSelectedContent;
    private Content mContentToSave;
    private long mNewContentButtonLastClickTime;

    // Views
    private View mMapButton;
    private View mWaterMark;
    private View mProfileBar;
    protected TipView mTipView;
    protected LoadingFab mNewContentButton;
    private SelectedMenuView mSelectedMenuView;
    private RajawaliSurfaceView mRajawaliView;

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

        mRajawaliView = (RajawaliSurfaceView) findViewById(R.id.rajawali_render_view);
        mSelectedMenuView = (SelectedMenuView) findViewById(R.id.selected_menu_view);
        mSelectedMenuView.setOnSelectedMenuActionListener(this);
        // Initialize managers
        mSocialUserManager = ((App) getApplicationContext()).getSocialUserManager(); //TODO get LoginManager from the Factory!

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
        mTipView = (TipView) findViewById(R.id.tip_view);
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("bla", newConfig.toString());
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

    /**
     * Callback from Visual Manager
     *
     * @param visualContent selected Visual content
     */
    @Override
    public void onVisualContentSelected(VisualContent visualContent) {
        Content content = null;
        if (visualContent != null) {
            content = visualContent.getContent();
        }
        // Now call method for content
        onContentSelected(content);
    }

    /**
     * Callback for content selection.
     * <p/>
     * Note that we might have many content types [VirtualNote, PuzzleNote ...]
     *
     * @param content user selected content
     */
    private void onContentSelected(final Content content) {
        mSelectedContent = content;
        if (App.getInstance().getSocialUserManager().isLoggedIn()) {
            runOnUiThread(new Runnable() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void run() {
                    if (mSelectedContent != content || mSelectedContent == null) {
                        return;
                    }
                    if (mSelectedContent.isPuzzle()) {
                        onPuzzleSelected();
                    } else {
                        onVirtualNoteSelected();
                    }
                }
            });
        }
    }

    private void onPuzzleSelected() {
        if (mSelectedContent.getPuzzle().isSolved()) {
            Toast.makeText(CameraARActivity.this, R.string.puzzle_is_already_solved, Toast.LENGTH_SHORT).show();
            return;
        }

        Date penaltyDate = App.getInstance().penaltyForPuzzle(mSelectedContent);
        if (penaltyDate == null) {
            // User has no penalty, let's make him answer the question
            PuzzleAnswerDialogFragment.newInstance().show(getSupportFragmentManager(), PuzzleAnswerDialogFragment.TAG);
        } else {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            String dateString = df.format(penaltyDate);
            String message = getString(R.string.puzzle_penalty_until_message) + " " + dateString;
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void onVirtualNoteSelected() {
        mSelectedMenuView.setVisibility(mSelectedContent == null ? View.GONE : View.VISIBLE);
        mSelectedMenuView.setContent(mSelectedContent, mGoogleApiClient);
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

    /**
     * Called whenever user answers for selected puzzle.
     * Note that answer might be correct or incorrect.
     *
     * @param answer user typed answer.
     */
    @Override
    public void onPuzzleAnswer(String answer) {
        if (mSelectedContent == null || !mSelectedContent.isPuzzle()) {
            Log.e(TAG, "onPuzzleAnswer: called when Content wasn't selected or content wasn't puzzle");
            return;
        }
        Puzzle puzzle = mSelectedContent.getPuzzle();
        if (((App) getApplicationContext()).getDataController().checkAnswer(puzzle, answer)) {
            // TODO here network call to tell server correct answer
            puzzle.withIsSolved(true);
            openMapFragment(MapsFragment.newInstance(puzzle));
            Toast.makeText(CameraARActivity.this, R.string.puzzle_correct_answer, Toast.LENGTH_SHORT).show();
        } else {
            App.getInstance().incorrectPenaltyTrial(mSelectedContent);
            Toast.makeText(CameraARActivity.this, R.string.puzzle_wrong_answer, Toast.LENGTH_SHORT).show();
        }
    }

    public void onNewContentClick(View v) {
        if (!isNewContentCreationEnabled()) {
            return;
        }
        if (SystemClock.elapsedRealtime() - mNewContentButtonLastClickTime < 1000) {
            return;
        }
        mNewContentButtonLastClickTime = SystemClock.elapsedRealtime();

        // If one can create puzzle, let's them decide if they want puzzle or note content.
        if (BuildConfig.CAN_CREATE_PUZZLE) {
            PersistentDialogFragment.newInstance(this,
                    RC_CHOOSE_PUZZLE_OR_NOTE,
                    R.string.select_puzzle_or_note_msg,
                    R.string.puzzle,
                    R.string.note).show(getSupportFragmentManager(), PersistentDialogFragment.TAG);
        } else {
            NewContentDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
        }
    }

    @CallSuper
    @Override
    public void onDialogPositiveClicked(int requestCode) {
        super.onDialogPositiveClicked(requestCode);
        if (RC_CHOOSE_PUZZLE_OR_NOTE == requestCode) {
            NewContentDialogFragment.newInstance(true)
                    .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
        }
    }

    @CallSuper
    @Override
    public void onDialogNegativeClicked(int requestCode) {
        super.onDialogNegativeClicked(requestCode);
        if (RC_CHOOSE_PUZZLE_OR_NOTE == requestCode) {
            NewContentDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
        }
    }

    public void onBtnMapClick(View v) {
        openMapFragment(MapsFragment.newInstance());
    }

    public void onShowProfileClick(View v) {
        onProfileClick(mSocialUserManager.getUser(), true);
    }


    public void onEditSelectedContentClick(Content content) {
        if (content == null) {
            Log.e(TAG, "editSelectedContent: when mSelectedContent is NULL");
            return;
        }
        Log.d(TAG, "onEditSelectedContentClick() called with: " + "content = [" + content + "]");
        NewContentDialogFragment.newInstance(content)
                .show(getSupportFragmentManager(), NewContentDialogFragment.TAG);
    }

    public void onDeleteSelectedContentClick(Content content) {
        if (content == null) {
            Log.e(TAG, "deleteSelectedContent: when mSelectedContent is NULL");
            return;
        }
        //delete content on the server
        ((App) getApplicationContext()).getDataController().delete(content);
        onDeleteContent(content);
    }

    public void onProfileClick(SocialUser user, boolean type) {
        openMapFragment(user);
    }

    @CallSuper
    @Override
    protected void onPermissionsGranted(int permissionCode) {
        if (permissionCode == RC_SAVE_CONTENT) {
            saveActiveContent(mContentToSave);
        }
    }

    protected void saveActiveContent(final Content content) {
        mContentToSave = content;
        if (!Utils.checkHasLocationPermission(this)) {
            requestPermissions(RC_SAVE_CONTENT);
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
                ((App) getApplicationContext()).getDataController().save(content);
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

    public void openMapFragment(SocialUser socialUser) {
        openMapFragment(MapsFragment.newInstance(socialUser));
    }

    public void openMapFragment(MapsFragment mf) {
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
