package com.wally.wally.controllers.map;

import android.support.v4.app.Fragment;

import com.wally.wally.BaseActivity;

/**
 * This BaseFragment helps you to manage base functionality used in our fragments.
 * <p/>
 * For Now It gives you following features:
 * <ul>
 * <li>Location permission</li>
 * To use Location Permission You must override method {@link #onLocationPermissionGranted(int)}
 * And call {@link #requestLocationPermission(int)} with your action code,
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
     * You will get result in method {@link #onLocationPermissionGranted(int)}
     * <p/>
     * We assume that this program doesn't work without location permission.
     * So if you can work without location permission and fall gracefully please <b>don't call this method</b>
     *
     * @param locationRequestCode request code that defines your unique action,
     *                            you will receive {@link #onLocationPermissionGranted(int)}
     *                            with same request code.
     * @see BaseActivity#requestLocationPermission(int)
     */
    final void requestLocationPermission(int locationRequestCode) {
        getParentActivity().requestLocationPermission(locationRequestCode);
    }

    /**
     * Called after location permission is granted.
     *
     * @param locationRequestCode request code that was passed when {@link #requestLocationPermission(int)}
     */
    public abstract void onLocationPermissionGranted(int locationRequestCode);

    private BaseActivity getParentActivity() {
        return (BaseActivity) getActivity();
    }
}
