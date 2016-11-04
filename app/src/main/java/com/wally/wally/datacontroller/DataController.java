package com.wally.wally.datacontroller;

import com.wally.wally.datacontroller.callbacks.PuzzleSharerCallback;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.Puzzle;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

class DataController implements DBController {
    private User currentUser;
    private ContentManager contentManager;
    private FetcherFactory fetcherFactory;

    DataController withCurrentUser(User user) {
        if (user == null) {
            String message = "DataController can't operate without current user";
            throw new IllegalArgumentException(message);
        }
        this.currentUser = user;
        return this;
    }

    DataController withContentManager(ContentManager manager) {
        this.contentManager = manager;
        return this;
    }

    DataController withFetcherFactory(FetcherFactory factory) {
        this.fetcherFactory = factory;
        return this;
    }

    public void save(Content c) {
        contentManager.save(c);
    }

    public void delete(Content c) {
        contentManager.delete(c);
    }

    public void fetchForUuid(String uuid, final ResultCallback callback) {
        contentManager.fetchForUuid(uuid, new ResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                Collection<Content> contents = new HashSet<>();
                for (Content c : result) {
                    if (isContentVisibleForUser(c, currentUser)) {
                        contents.add(c);
                    }
                }
                callback.onResult(contents);
            }
        });
    }

    public Fetcher createFetcherForPuzzleSuccessors(Puzzle puzzle) {
        PagerChain chain = new PagerChain();
        for (final String puzzleId : puzzle.getSuccessors()) {
            chain.addPager(new Fetcher() {
                @Override
                public void fetchNext(int i, ResultCallback callback) {
                    ResultCallback sharerCallback = new PuzzleSharerCallback(callback);
                    contentManager.fetchAt(puzzleId.replace(":", "/"), sharerCallback);
                }
            });
        }
        return chain;
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
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForPrivate(currentUser));
        chain.addPager(fetcherFactory.createForSharedByMe(currentUser));
        chain.addPager(fetcherFactory.createForPublic(currentUser));
        return chain;
    }

    public Fetcher createFetcherForVisibleContent(double centerLat, double centerLng, double radiusKm) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(currentUser,
                centerLat, centerLng, radiusKm));
        chain.addPager(fetcherFactory.createForPublic(centerLat, centerLng, radiusKm));
        return chain;
    }

    public Fetcher createFetcherForUserContent(User user) {
        PagerChain chain = new PagerChain();
        chain.addPager(fetcherFactory.createForSharedWithMe(currentUser, user));
        chain.addPager(fetcherFactory.createForPublic(user));
        return chain;
    }

    private boolean isContentVisibleForUser(Content c, User user) {
        List<Id> sharedWith = c.getVisibility().getSocialVisibility().getSharedWith();
        return c.getAuthorId().equals(user.getId().getId()) ||
                c.isPublic() || (c.isPrivate() && c.getAuthorId().equals(user.getId().getId())) ||
                sharedWith.contains(user.getId()) ||
                sharedWith.contains(user.getFbId()) ||
                sharedWith.contains(user.getGgId());
    }
}