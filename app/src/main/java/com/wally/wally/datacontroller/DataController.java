package com.wally.wally.datacontroller;

import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.Puzzle;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.wally.wally.datacontroller.DataControllerFactory.getUserManagerInstance;

public class DataController {
    private ContentManager contentManager;
    private FetcherFactory fetcherFactory;

    public DataController withContentManager(ContentManager manager) {
        this.contentManager = manager;
        return this;
    }

    public DataController withFetcherFactory(FetcherFactory factory) {
        this.fetcherFactory = factory;
        return this;
    }

    public void save(Content c) {
        contentManager.save(c);
    }

    public void delete(Content c) {
        contentManager.delete(c);
    }

    public void fetchForUuid(String uuid, final FetchResultCallback callback) {
        contentManager.fetchForUuid(uuid, new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                Collection<Content> contents = new HashSet<>();
                for (Content c : result) {
                    if (isContentVisibleForUser(c, getUserManagerInstance().getCurrentUser())) {
                        contents.add(c);
                    }
                }
                callback.onResult(contents);
            }
        });
    }

    private boolean isContentVisibleForUser(Content c, User user) {
        List<Id> sharedWith = c.getVisibility().getSocialVisibility().getSharedWith();
        return c.isPublic() || (c.isPrivate() && c.getAuthorId().equals(user.getId().getId())) ||
                sharedWith.contains(user.getId()) ||
                sharedWith.contains(user.getFbId()) ||
                sharedWith.contains(user.getGgId());
    }

    public Fetcher createFetcherForPuzzleSuccessors(Puzzle puzzle) {
        return null;
    }

    public boolean checkAnswer(Puzzle puzzle, String answer) {
        answer = answer.toLowerCase();
        for (String s : puzzle.getAnswers()) {
            if (s.toLowerCase().equals(answer)) {
                return true;
            }
        }
        return false;
    }

    public Fetcher createFetcherForMyContent() {
        User current = getUserManagerInstance().getCurrentUser();
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForPrivate(current));
        chain.addPager(fetcherFactory.createForSharedByMe(current));
        chain.addPager(fetcherFactory.createForPublic(current));
        return chain;
    }

    public Fetcher createFetcherForVisibleContent(SerializableLatLng center, double radiusKm) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(
                getUserManagerInstance().getCurrentUser(), center, radiusKm));
        chain.addPager(fetcherFactory.createForPublic(center, radiusKm));
        return chain;
    }

    public Fetcher createFetcherForUserContent(User user) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(
                getUserManagerInstance().getCurrentUser(), user));
        chain.addPager(fetcherFactory.createForPublic(user));
        return chain;
    }

    public interface FetchResultCallback {
        void onResult(Collection<Content> result);
    }

    public interface Fetcher {
        void fetchNext(int i, FetchResultCallback callback);
    }
}