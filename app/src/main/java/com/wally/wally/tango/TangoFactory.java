package com.wally.wally.tango;

import android.content.Context;
import com.google.atap.tangoservice.Tango;


/**
 * Created by shota on 5/28/16.
 */
public class TangoFactory {
    private Context mContext;

    public TangoFactory(Context context){
        mContext = context;
    }

    public Tango getTango(Runnable r){
        return new Tango(mContext, r);
    }
}
