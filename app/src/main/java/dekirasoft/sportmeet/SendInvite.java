package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Kota on 1/8/2015.
 */
public class SendInvite extends Activity {

    Button review;
    ImageView calender;
    TextView eventDate, year, cancel;
    TimePicker timePicker;
    EditText address, notes;
    private int pYear;
    private int pMonth;
    private int pDay;
    DatePickerDialog datePickerDialog;
    String setDate, mday, myear, location, userId;
    static final int DATE_DIALOG_ID = 0;

    /**
     * Callback received when the user "picks" a date in the dialog
     */
    private DatePickerDialog.OnDateSetListener pDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    pYear = year;
                    pMonth = monthOfYear;
                    pDay = dayOfMonth;
                    updateDisplay();
                    displayToast();
                }
            };

    /**
     * Updates the date in the TextView
     */
    private void updateDisplay() {

        eventDate.setText(
                new StringBuilder()
                        .append(pDay).append(" ")
                        .append(formatMonth(pMonth, Locale.getDefault()).substring(0, 3)).append(""));

        year.setText(new StringBuilder()
                .append(" ").append(pYear));

    }

    /**
     * Displays a notification when the date is updated
     */
    private void displayToast() {
        Toast.makeText(this, new StringBuilder().append("Date chosen is ").append(eventDate.getText()).append(year.getText()), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.send_invite);

        cancel = (TextView) findViewById(R.id.cancel);
        review = (Button) findViewById(R.id.review);

        final Intent main = new Intent(this, MainActivity.class);
        calender = (ImageView) findViewById(R.id.dateSelect);
        eventDate = (TextView) findViewById(R.id.date);
        year = (TextView) findViewById(R.id.year);
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        address = (EditText) findViewById(R.id.inviteAddress);
        notes = (EditText) findViewById(R.id.notes);
        //get current day for DateTime date;
        timePicker.setIs24HourView(Boolean.FALSE);

        //get intent
        try {
            Intent in = getIntent();
            userId = in.getStringExtra("UserId");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Clicks
        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        /** Get the current date */
        final Calendar cal = Calendar.getInstance();
        pYear = cal.get(Calendar.YEAR);
        pMonth = cal.get(Calendar.MONTH);
        pDay = cal.get(Calendar.DATE);

        /** Display the current date in the TextView */
        updateDisplay();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(main);
                finish();
            }
        });

        final Intent o = new Intent(this, ReviewInvite.class);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventDate.getText().toString() != "") {
                    location = address.getText().toString();
                    String time = timePicker.getCurrentHour().toString() + ":" +  timePicker.getCurrentMinute().toString();
                    DateFormat format = new SimpleDateFormat("HH:mm");
                    DateFormat output = new SimpleDateFormat("KK:mm a");
                    try {
                        time = output.format(format.parse(time));
                        System.out.println(time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    System.out.println(time);
                    o.putExtra("location", location);
                    o.putExtra("notes", notes.getText().toString());
                    o.putExtra("time", time);
                    o.putExtra("year", year.getText().toString());
                    o.putExtra("day", eventDate.getText().toString());
                    o.putExtra("sport", MyResources.sportInSearch);
                    o.putExtra("userId", userId);
                    startActivity(o);

                } else {
                    Toast.makeText(v.getContext(), "Please make sure a date is selected", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }

    /**
     * Create a new dialog for date picker
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                datePickerDialog = new DatePickerDialog(this,
                        pDateSetListener,
                        pYear, pMonth, pDay);
                datePickerDialog.getDatePicker().setMinDate(Calendar.YEAR);
                return datePickerDialog;
        }
        return null;
    }

    public String formatMonth(int month, Locale locale) {
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        String[] monthNames = symbols.getMonths();
        return monthNames[month];
    }
}
