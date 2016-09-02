package com.wally.wally.controllers;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.wally.wally.R;

/**
 * This class manages user answer Dialog for puzzle
 * Make sure that class that uses this Dialog implements {@link PuzzleAnswerListener} interface
 * <p>
 * Created by ioane5 on 9/2/16.
 */
public class PuzzleAnswerDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = PuzzleAnswerDialogFragment.class.getSimpleName();
    private EditText mAnswer;

    public static PuzzleAnswerDialogFragment newInstance() {
        return new PuzzleAnswerDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams") View dv = LayoutInflater.from(getActivity())
                .inflate(R.layout.puzzle_answer_dialog, null, false);
        initViews(dv);

        builder.setView(dv);
        Dialog dialog = builder.create();
        setCancelable(true);
        return dialog;
    }

    private void initViews(View dv) {
        mAnswer = (EditText) dv.findViewById(R.id.et_answer);
        dv.findViewById(R.id.btn_answer).setOnClickListener(this);
        dv.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_cancel) {
            dismiss();
        } else if (view.getId() == R.id.btn_answer) {
            dismiss();
            ((PuzzleAnswerListener) getActivity()).onPuzzleAnswer(mAnswer.getText().toString());
        }
    }

    public interface PuzzleAnswerListener {
        /**
         * Called when user types answer in Text field and presses button answer.
         *
         * @param answer user typed answer.
         */
        void onPuzzleAnswer(String answer);
    }
}
