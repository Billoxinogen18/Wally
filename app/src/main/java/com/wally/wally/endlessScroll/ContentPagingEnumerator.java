package com.wally.wally.endlessScroll;

/**
 * Created by Meravici on 6/22/2016.
 */
public class ContentPagingEnumerator {
    private int pageLength;

    private int start;
    private int end;


    public ContentPagingEnumerator(int pageLength){
        this.pageLength = pageLength;

        start = 1;
        end = pageLength+1;
    }


    public int get(int i){
        return start + i;
    }

    public void next(int num){
        start += num;
//        end +=
    }

    public void prev(int num){
        start -= num;
    }

}
