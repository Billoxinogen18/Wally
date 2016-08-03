package com.wally.wally.datacontroller;

import static com.wally.wally.datacontroller.DataControllerFactory.getUserManagerInstance;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.ContentManager;
import com.wally.wally.datacontroller.fetchers.PagerChain;
import com.wally.wally.datacontroller.user.User;
import com.wally.wally.datacontroller.utils.SerializableLatLng;

public class DataController {
    private ContentManager contentManager;
    private ContentFetcherFactory fetcherFactory;

    public DataController withContentManager(ContentManager manager) {
        this.contentManager = manager;
        return this;
    }

    public DataController withFetcherFactory(ContentFetcherFactory factory) {
        this.fetcherFactory = factory;
        return this;
    }

    public void save(Content c) {
        contentManager.save(c);
    }

    public void delete(Content c) {
        contentManager.delete(c);
    }

    public void fetchForUuid(String uuid, FetchResultCallback callback) {
        contentManager.fetchForUuid(uuid, callback);
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


    public interface  Fetcher {
        void fetchNext(int i, FetchResultCallback callback);
    }
}