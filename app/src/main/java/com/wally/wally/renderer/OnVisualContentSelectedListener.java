package com.wally.wally.renderer;

import com.wally.wally.renderer.VisualContent;

/**
 * Interface for listening VisualContent selects
 * Created by ioane5 on 4/27/16.
 */
public interface OnVisualContentSelectedListener {

    /**
     * Called when content is selected
     *
     * @param c content
     */
    void onVisualContentSelected(VisualContent c);

}
