package com.wally.wally.endlessScroll;

import android.os.Handler;

import com.wally.wally.controllers.map.contentList.PagingRetriever;
import com.wally.wally.datacontroller.DBController.Fetcher;
import com.wally.wally.datacontroller.DBController.ResultCallback;
import com.wally.wally.objects.content.Content;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Created by Meravici on 5/24/16. yea
 */

public class ContentPagingRetrieverTest {
    private PagingRetriever mContentRetriever;
    private Fetcher contentFetcher;

    @Before
    public void init() {
        contentFetcher = mock(Fetcher.class);
        doAnswer(getNextAnswer(0)).when(contentFetcher).fetchNext(anyInt(), any(ResultCallback.class));
        mContentRetriever = new PagingRetriever(contentFetcher, getLinearHandler(), 2);
    }


    @After
    public void finish() {
        contentFetcher = null;
        mContentRetriever = null;
    }

    @Test
    public void initialFetchTest() {

        assertEquals(6, mContentRetriever.size());
        for (int i = 0; i < 6; i++) {
            assertEquals("" + i, mContentRetriever.get(i).getTitle());
        }
    }


    @Test
    public void NextTest() {
        doAnswer(getNextAnswer(6))
                .doAnswer(getNextAnswer(8))
                .when(contentFetcher).fetchNext(anyInt(), any(ResultCallback.class));

        mContentRetriever.loadNext();

        assertEquals(6, mContentRetriever.size());
        for (int i = 0; i < 6; i++) {
            assertEquals("" + (i + 2), mContentRetriever.get(i).getTitle());
        }

        mContentRetriever.loadNext();

        assertEquals(6, mContentRetriever.size());
        for (int i = 0; i < 6; i++) {
            assertEquals("" + (i + 4), mContentRetriever.get(i).getTitle());
        }
    }

    @Test
    public void NextEndTest() {
        doAnswer(getEmptyAnswer()).when(contentFetcher).fetchNext(anyInt(), any(ResultCallback.class));

        assertEquals(6, mContentRetriever.size());
        for (int i = 0; i < 6; i++) {
            assertEquals("" + i, mContentRetriever.get(i).getTitle());
        }

        mContentRetriever.loadNext();

        assertEquals(6, mContentRetriever.size());
        for (int i = 0; i < 6; i++) {
            assertEquals("" + i, mContentRetriever.get(i).getTitle());
        }
    }


    private Answer getNextAnswer(final int start) {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                int num = (Integer) invocation.getArguments()[0];
                ResultCallback callback = (ResultCallback) invocation.getArguments()[1];

                ArrayList<Content> result = new ArrayList<>(num);
                for (int i = start; i < start + num; i++) {
                    Content content = new Content("asd").withTitle("" + i);
                    result.add(content);
                }

                callback.onResult(result);
                return null;
            }
        };
    }

    private Answer getEmptyAnswer() {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ResultCallback callback = (ResultCallback) invocation.getArguments()[1];

                ArrayList<Content> result = new ArrayList<>();

                callback.onResult(result);
                return null;
            }
        };
    }

    private Handler getLinearHandler() {
        Handler handler = mock(Handler.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(handler).post(any(Runnable.class));
        return handler;

    }
}
