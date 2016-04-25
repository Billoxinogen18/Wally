package com.wally.wally.activities;

import android.support.annotation.Nullable;


import com.wally.wally.datacontroller.dal.Callback;
import com.wally.wally.datacontroller.Content;

import java.util.Collection;

/**
 * Created by Xato on 4/8/2016.
 */
public abstract class EnumCallback implements Callback<Collection<Content>> {
    private long id;
    public EnumCallback(long id){
        this.id = id;
    }

    public long getId(){
        return id;
    }
    @Override
    public abstract void call(@Nullable Collection<Content> result, @Nullable Exception e);
}
