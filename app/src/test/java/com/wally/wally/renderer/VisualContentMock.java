package com.wally.wally.renderer;

import android.support.annotation.NonNull;

import com.wally.wally.datacontroller.content.Content;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * Created by shota on 5/25/16.
 */
public class VisualContentMock extends VisualContent {

    public VisualContentMock(@NonNull Content content) {
        super(content);
    }


    public static VisualContent getVisualContent(){
        Objenesis objenesis = new ObjenesisStd(); // or ObjenesisSerializer
        VisualContentMock res = (VisualContentMock) objenesis.newInstance(VisualContentMock.class);

        return res;
    }

}
