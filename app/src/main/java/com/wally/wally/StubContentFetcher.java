package com.wally.wally;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.DebugUtils;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Xato on 6/20/2016.
 */
public class StubContentFetcher implements ContentFetcher {
    public ArrayList<Content> contents= new ArrayList<>();
    private ListIterator<Content> iterator;
    private static SecureRandom random = new SecureRandom();

    {
        for (int i = 0; i < 100; i++) {
            Content content = generateRandomContent().withTitle("" + i);
            content.getVisibility().withSocialVisibility(Visibility.PUBLIC).withAnonymousAuthor(false).withVisiblePreview(true);
            contents.add(content);
        }

        iterator = contents.listIterator();
    }

    @Override
    public void fetchPrev(int i, final FetchResultCallback callback) {
        final ArrayList<Content> res = new ArrayList<>();
        
        for(int y = 0; y < i; y++){
            if(!iterator.hasPrevious()) break;
            res.add(iterator.previous());
        }
        Collections.reverse(res);
        Log.d("BLA", "fetchPrev: " + listToString(res));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.onResult(res);
            }
        }).start();
    }

    @Override
    public void fetchNext(int i, final FetchResultCallback callback) {
        final ArrayList<Content> res = new ArrayList<>();
        for(int y = 0; y < i; y++){
            if(!iterator.hasNext()) break;
            res.add(iterator.next());
        }
        Log.d("BLA", "fetchNext: " + listToString(res));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.onResult(res);
            }
        }).start();
    }

    private String listToString(ArrayList<Content> res) {
        StringBuilder sb = new StringBuilder();
        for (Content content : res) {
            sb.append(content.getTitle()).append(",");
        }

        return sb.toString();
    }

    private static Content generateRandomContent() {
        return new Content()
                .withUuid(DebugUtils.randomStr(1))
                .withNote(DebugUtils.randomStr(15))
//                .withTitle(DebugUtils.randomStr(7))
                .withColor(random.nextInt())
//                .withImageUri("http://" + DebugUtils.randomStr(10))
                .withAuthorId("8g7t26liJZgP6Z7jHgTkTdZLk632")
                .withLocation(
                        new LatLng(
                                random.nextDouble(),
                                random.nextDouble()
                        )
                ).withVisibility(
                        new Visibility()
                                .withTimeVisibility(null)
                                .withAnonymousAuthor(false)
                                .withSocialVisibility(DebugUtils.randomPublicity())
                                .withVisiblePreview(random.nextBoolean())
                                .withAnonymousAuthor(random.nextBoolean())
                ).withTangoData(
                        new TangoData()
                                .withScale((double) random.nextInt())
                                .withRotation(DebugUtils.randomDoubleArray())
                                .withTranslation(DebugUtils.randomDoubleArray()));
    }
}
