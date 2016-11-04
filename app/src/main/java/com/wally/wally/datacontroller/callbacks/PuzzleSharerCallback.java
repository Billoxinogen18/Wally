package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.DataController.FetchResultCallback;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.objects.content.Content;
import com.wally.wally.datacontroller.user.Id;

import java.util.Collection;


public class PuzzleSharerCallback implements FetchResultCallback {
    private final FetchResultCallback innerCallback;
    private final Id id;

    public PuzzleSharerCallback(FetchResultCallback callback) {
        this.innerCallback = callback;
        id = DataControllerFactory.getUserManagerInstance().getCurrentUser().getId();
    }

    @Override
    public void onResult(Collection<Content> result) {
        for (Content c : result) {
            c.getVisibility().getSocialVisibility().getSharedWith().add(id);
            DataControllerFactory.getDataControllerInstance().save(c);
        }
        innerCallback.onResult(result);
    }
}
