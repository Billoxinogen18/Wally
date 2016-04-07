package com.wally.wally.dal;

import android.support.annotation.NonNull;

import com.wally.wally.dal.content.Content;
import com.wally.wally.dal.content.Location;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DummyDAL implements DataAccessLayer<Content> {
    private Set<Content> db;
    private Location baseLocation;

    public DummyDAL(int nContents) {
        db = new HashSet<>();
        for (int i = 0; i < nContents; i++) {
            generateDummyContent();
        }
        baseLocation = new Location(41.71196838230613, 44.75304298102856);
        Content baseContent = new Content();
        baseContent.setLocation(baseLocation);
        db.add(baseContent);
    }

    public DummyDAL() {
        this(1000);
    }

    private void generateDummyContent(){
        Random rand = new Random();
        double lat = baseLocation.getLatitude() + rand.nextDouble()/100;
        double lng = baseLocation.getLongitude() + rand.nextDouble()/100;
        Content content = new Content();
        content.setLocation(new Location(lat, lng));
        save(content);
    }

    @Override
    public void save(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        db.add(c);
        statusCallback.call(true, null);
    }

    @Override
    public void save(@NonNull Content c) {
        db.add(c);
    }

    @Override
    public void delete(@NonNull Content c, @NonNull Callback<Boolean> statusCallback) {
        db.remove(c);
        statusCallback.call(true, null);
    }

    @Override
    public void delete(@NonNull Content c) {
        db.remove(c);
    }

    @Override
    public void fetch(@NonNull Query query, @NonNull Callback<Collection<Content>> resultCallback) {
        resultCallback.call(new HashSet<>(db), null);
    }
}
