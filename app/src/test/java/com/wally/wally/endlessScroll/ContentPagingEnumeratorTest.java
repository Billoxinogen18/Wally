package com.wally.wally.endlessScroll;

import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Meravici on 6/22/2016. yea
 */
public class ContentPagingEnumeratorTest {
    private ContentPagingEnumerator contentPagingEnumerator;

    @Before
    public void init() {
        contentPagingEnumerator = new ContentPagingEnumerator(2);
    }

    @Test
    public void initialTest() {
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, contentPagingEnumerator.get(i));
        }
    }

    @Test
    public void nextTest() {
        contentPagingEnumerator.next(2);
        for (int i = 0; i < 6; i++) {
            assertEquals(i+3, contentPagingEnumerator.get(i));
        }
    }

    @Test
    public void multipleNextTest() {
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);
        for (int i = 0; i < 6; i++) {
            assertEquals(i+7, contentPagingEnumerator.get(i));
        }
    }


    @Test
    public void NextAfterPrevious(){
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.prev(2);

        for (int i = 0; i < 6; i++) {
            assertEquals(i+1, contentPagingEnumerator.get(i));
        }
    }

    @Test
    public void previousTest() {
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);

        contentPagingEnumerator.prev(2);

        for (int i = 0; i < 6; i++) {
            assertEquals(i+5, contentPagingEnumerator.get(i));
        }
    }

    @Test
    public void multiplePreviousTest() {
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);
        contentPagingEnumerator.next(2);

        contentPagingEnumerator.prev(2);
        contentPagingEnumerator.prev(2);

        for (int i = 0; i < 6; i++) {
            assertEquals(i+3, contentPagingEnumerator.get(i));
        }
    }


    @After
    public void finish() {
        contentPagingEnumerator = null;
    }
}
