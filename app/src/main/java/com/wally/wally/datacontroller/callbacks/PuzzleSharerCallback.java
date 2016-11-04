package com.wally.wally.datacontroller.callbacks;

import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.datacontroller.DataControllerFactory;
import com.wally.wally.objects.content.Content;
import com.wally.wally.datacontroller.user.Id;

import java.util.Collection;


public class PuzzleSharerCallback implements ResultCallback {
    private final ResultCallback innerCallback;
    private final Id id;

    public PuzzleSharerCallback(ResultCallback callback) {
        this.innerCallback = callback;
        id = DataControllerFactory.getUserManagerInstance().getCurrentUser().getId();
    }

    @Override
    public void onResult(Collection<Content> result) {
        for (Content c : result) {
            c.getVisibility().getSocialVisibility().getSharedWith().add(id);
//            DataControllerFactory.getDataControllerInstance().save(c);
            // TODO resolve issue without breaking architecture
        }
        innerCallback.onResult(result);
    }
}
