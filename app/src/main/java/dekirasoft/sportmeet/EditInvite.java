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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.sql.Time;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Kota on 1/8/2015.
 */
public class EditInvite extends Activity {

    Button review;
    ImageView calender;
    TextView eventDate, year, cancel;
    TimePicker timePicker;
    EditText address, notes;
    Time date;
    String setDate, mday, myear, location, time, day, yearText, sportPlaying, objId;

    private int pYear;
    private int pMonth;
    private int pDay;
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
                        .append(formatMonth(pMonth, Locale.getDefault()).substring(0, 3)).append(" "));

        year.setText(new StringBuilder()
                .append(pYear));

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

        //Define xml elements
        cancel = (TextView) findViewById(R.id.cancel);
        review = (Button) findViewById(R.id.review);
        calender = (ImageView) findViewById(R.id.dateSelect);
        eventDate = (TextView) findViewById(R.id.date);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        year = (TextView) findViewById(R.id.year);
        address = (EditText) findViewById(R.id.inviteAddress);
        notes = (EditText) findViewById(R.id.notes);

        //Retrieve intent
        try {
            Intent intent = getIntent();
            location = intent.getStringExtra("location");

            time = intent.getStringExtra("time");
            day = intent.getStringExtra("day");
            yearText = intent.getStringExtra("year");
            sportPlaying = intent.getStringExtra("sport");
            objId = intent.getStringExtra("objId");

            System.out.println(time + day + yearText + sportPlaying + objId);


        } catch (Exception e) {
            e.printStackTrace();
        }


        //Populate views
//        eventDate.setText(day);
//        year.setText(yearText);
        eventDate.setText(day);
        year.setText(yearText);
        if (!location.isEmpty()) {
            address.setText(location);
        }
        timePicker.setIs24HourView(Boolean.FALSE);

        //Clicks
        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        final Intent o = new Intent(this, UserInvites.class);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                location = address.getText().toString();
                String time = timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute();
                ParseQuery query = ParseQuery.getQuery("Invite");
                ParseObject obj = null;
                try {
                    obj = query.get(objId);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (!location.isEmpty()) obj.put("location", location);
                if (!time.isEmpty()) obj.put("time", time);
                if (year.getText().toString().trim().isEmpty())
                    obj.put("dateOf", eventDate.getText().toString());
                obj.saveInBackground();

                startActivity(o);

            }
        });
        final Intent home = new Intent(this, UserInvites.class);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(home);
                finish();
            }
        });

        /** Get the current date */
        final Calendar cal = Calendar.getInstance();
        pYear = cal.get(Calendar.YEAR);
        pMonth = cal.get(Calendar.MONTH);
        pDay = cal.get(Calendar.DAY_OF_MONTH);

        /** Display the current date in the TextView */
        updateDisplay();

    }

    /**
     * Create a new dialog for date picker
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        pDateSetListener,
                        pYear, pMonth, pDay);
        }
        return null;
    }

    public String formatMonth(int month, Locale locale) {
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        String[] monthNames = symbols.getMonths();
        return monthNames[month];
    }
}
