package com.wally.wally.components;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * This is adapter class for simple use case.
 * When you just want to have simple interface for getting text changes.
 * {@link TextWatcher} is too ugly to use, and has many unnecessary methods for us.
 * </p>
 * Created by ioane5 on 6/30/16.
 */
public class TextChangeListenerAdapter implements TextWatcher {

    private TextChangeListener mListener;

    public TextChangeListenerAdapter(TextChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mListener.onTextChanged();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public interface TextChangeListener {
        void onTextChanged();
    }
}
