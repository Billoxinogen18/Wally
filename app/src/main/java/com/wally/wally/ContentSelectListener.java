package com.wally.wally;

import com.wally.wally.datacontroller.content.Content;

/**
 * Interface for listening content selects
 * Created by ioane5 on 4/27/16.
 */
public interface ContentSelectListener {

    /**
     * Called when content is selected
     *
     * @param c content
     */
    void onContentSelected(Content c);

}
