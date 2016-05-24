package com.wally.wally.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.CalendarView;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Dialog that makes user pick date.
 * <p/>
 * <i>Note that class that calls this Dialog <b>must</b> implement </i> {@link DatePickListener}
 * Created by ioane5 on 5/12/16.
 */
public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static final String TAG = DatePickerDialogFragment.class.getSimpleName();

    public static DatePickerDialogFragment newInstance() {
        return new DatePickerDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);

        DatePicker dp = dialog.getDatePicker();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            dp.setMinDate(System.currentTimeMillis() - 1000);
        } else {
            // TODO remove this when we upgrade project to target marshmallow
            CalendarView cv = dp.getCalendarView();
            // FIX buggy datepicker behaviour
            // Link to solution http://stackoverflow.com/a/19125686/4183017
            long cur = cv.getDate();
            int d = cv.getFirstDayOfWeek();
            dp.setMinDate(System.currentTimeMillis() - 1000);
            cv.setDate(cur + 1000L * 60 * 60 * 24 * 40);
            cv.setFirstDayOfWeek((d + 1) % 7);
            cv.setDate(cur);
            cv.setFirstDayOfWeek(d);
        }
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        DatePickListener listener;
        if (getParentFragment() != null) {
            listener = (DatePickListener) getParentFragment();
        } else {
            listener = (DatePickListener) getActivity();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        listener.onDateSelected(calendar.getTime());
    }

    public interface DatePickListener {

        void onDateSelected(Date selectedDate);
    }
}