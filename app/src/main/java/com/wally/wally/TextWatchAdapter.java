package com.wally.wally;

import android.text.Editable;
import android.text.TextWatcher;

public class TextWatchAdapter implements TextWatcher {

    private SearchWatch mSearchWatcher;

    public TextWatchAdapter(SearchWatch searchWatch) {
        mSearchWatcher = searchWatch;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchWatcher.onNewQuery(s == null ? null : s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public interface SearchWatch {
        void onNewQuery(String query);
    }
}
