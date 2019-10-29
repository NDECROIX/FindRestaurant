package com.decroix.nicolas.go4lunch.controller.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.decroix.nicolas.go4lunch.R;

/**
 * Create TimePickerDialog with the time passed in the constructor
 */
class TimePickerFragment extends DialogFragment {

    private final TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private final int hours;
    private final int minutes;

    TimePickerFragment(TimePickerDialog.OnTimeSetListener onTimeSetListener, int hours, int minutes) {
        this.onTimeSetListener = onTimeSetListener;
        this.hours = hours;
        this.minutes = minutes;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), R.style.dateTimePicker,
                onTimeSetListener, hours, minutes, DateFormat.is24HourFormat(getActivity()));
    }
}
