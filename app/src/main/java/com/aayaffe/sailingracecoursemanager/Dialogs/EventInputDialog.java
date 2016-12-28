package com.aayaffe.sailingracecoursemanager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.aayaffe.sailingracecoursemanager.R;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;

import java.util.Calendar;

/**
 * Created by aayaffe on 09/02/2016.
 */
public class EventInputDialog extends DialogFragment {
    private static final String TAG = "EventInputDialog";
    public String eventName;
    public int yearStart;
    public int yearEnd;
    public int monthStart;
    public int monthEnd;
    public int dayStart;
    public int dayEnd;
    private Context c;
    public static EventInputDialog newInstance(String eventName, Context c) {
        EventInputDialog frag = new EventInputDialog();
        Bundle args = new Bundle();
        args.putString("eventName", eventName);
        frag.setArguments(args);
        frag.c = c;
        return frag;

    }
    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EventInputDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }
    // Use this instance of the interface to deliver action events
    EventInputDialogListener mListener;
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EventInputDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            Log.e(TAG,"Exception",e);
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        eventName = getArguments().getString("eventName", "");
        String title = "Add new Event";//(buoy_id==-1)?"Add BUOY":"Edit BUOY: "+ buoy_id;

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater)c.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.event_input_dialog, null);
        Button b = (Button) v.findViewById(R.id.selectDateRange);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
                                yearStart = year;
                                EventInputDialog.this.yearEnd = yearEnd;
                                monthStart=  monthOfYear;
                                monthEnd = monthOfYearEnd;
                                dayStart = dayOfMonth;
                                dayEnd = dayOfMonthEnd;
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        builder.setView(v)
        /*builder.setView(R.layout.event_input_dialog)*/
                .setTitle(title)
                // Add action buttons
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(EventInputDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EventInputDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
