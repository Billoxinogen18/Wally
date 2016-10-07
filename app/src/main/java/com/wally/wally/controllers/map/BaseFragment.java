package com.wally.wally.controllers.map;

import android.support.v4.app.Fragment;

import com.wally.wally.BaseActivity;

/**
 * This BaseFragment helps you to manage base functionality used in our fragments.
 * <p/>
 * For Now It gives you following features:
 * <ul>
 * <li>Grant runtime Permissions (Camera, Storage, Location</li>
 * You must override method {@link #onPermissionsGranted(int)}
 * And call {@link #requestPermissions(int)} with your action code,
 * you will get result with same code so you will be able to call the method again.
 * </ul>
 * </p>
 * <b>Note</b> that The fragments that use this activity must use {@link BaseActivity}.
 * Created by ioane5 on 8/5/16.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Call this method to request permission for your activity.
     * This will manage permission explanation and more.
     * You will get result in method {@link #onPermissionsGranted(int)}
     * <p/>
     * We assume that this program doesn't work without location permission.
     * So if you can work without location permission and fall gracefully please <b>don't call this method</b>
     *
     * @param permissionRequestCode request code that defines your unique action,
     *                            you will receive {@link #onPermissionsGranted(int)}
     *                            with same request code.
     * @see BaseActivity#requestPermissions(int)
     */
    final void requestPermissions(int permissionRequestCode) {
        getParentActivity().requestPermissions(permissionRequestCode);
    }

    /**
     * Called after location permission is granted.
     *
     * @param locationRequestCode request code that was passed when {@link #requestPermissions(int)}
     */
    public abstract void onPermissionsGranted(int locationRequestCode);

    private BaseActivity getParentActivity() {
        return (BaseActivity) getActivity();
    }
}
