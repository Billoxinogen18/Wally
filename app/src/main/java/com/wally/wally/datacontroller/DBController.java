package com.wally.wally.datacontroller;

import com.wally.wally.datacontroller.user.User;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.Puzzle;
import com.wally.wally.objects.content.SerializableLatLng;

import java.util.Collection;

/**
 * Created by Viper on 11/4/2016.
 * Unified interface through which front end
 * should interact with database
 */

public interface DBController {

    void save(Content content);

    void delete(Content content);

    void fetchForUuid(String adfUuid, ResultCallback resultCallback);

    boolean checkAnswer(Puzzle puzzle, String answer);

    Fetcher createFetcherForPuzzleSuccessors(Puzzle puzzle);

    Fetcher createFetcherForMyContent();

    Fetcher createFetcherForUserContent(User baseUser);

    Fetcher createFetcherForVisibleContent(SerializableLatLng center, double radius);

    interface ResultCallback {
        void onResult(Collection<Content> result);
    }

    interface Fetcher {
        void fetchNext(int i, ResultCallback callback);
    }
}
