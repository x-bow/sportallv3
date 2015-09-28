package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class NewUserSignUp extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static ParseGeoPoint geo;
    public static String city, state;
    public static TextView _addressText;
    List<String> sports, msports;
    private String name, photoUrl, email;
    ParseUser user;
    private List<ParseObject> rankingArray;
    TextView updateNewUser, cancelEdit;
    RadioButton male, female;
    private boolean mIsInResolution, userNew;
    protected static final int REQUEST_CODE_RESOLUTION = 1; //Request code for auto Google Play Services error resolution.
    private GoogleApiClient mGoogleApiClient;  //Google API client.

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.new_user_sign_up);


        //Create new instance of ranking array
        rankingArray = new ArrayList<ParseObject>();

        Intent intent = getIntent();
        userNew = intent.getBooleanExtra("userNew", false);

        male = (RadioButton)findViewById(R.id.male);
        female = (RadioButton)findViewById(R.id.female);
        TextView updateNewUser = (TextView) findViewById(R.id.updateNewUserProfile);
        Button selectSports = (Button) findViewById(R.id.selectSports);
        _addressText = (TextView) findViewById(R.id.newUserLocation);
        msports = new ArrayList<String>();

        if(!userNew) {

            System.out.println("User is not null");
            user = ParseUser.getCurrentUser();
            if (user.containsKey("sports")) {
                sports = MyResources.currentUserSports;
                for(String i : sports) {
                    System.out.println(i);
                }
            }

            try {
                if (user.containsKey("city")) {
                    city = user.getString("city");
                    _addressText.setText(city);
                    state = user.getString("state");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (user.containsKey("location")) {
                geo = user.getParseGeoPoint("location");
            }

            if (user.containsKey("gender")) {
                setGender(user.getString("gender"));
            }
        } else {

            System.out.println("user is null");

            name = intent.getStringExtra("name");
            photoUrl = intent.getStringExtra("photoUrl");
            email = intent.getStringExtra("email");

        }
        if (MyResources.editUserCity != null && MyResources.editUserCity != "") {
            city = MyResources.editUserCity;
            MyResources.editUserCity = "";
            _addressText.setText(city);
            geo = MyResources.geoAddress;
            System.out.println(geo.toString() + _addressText + city);
        }

        final Button updateLocation = (Button) findViewById(R.id.updateLocation);
        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildGoogleApiClient();
            }
        });

        selectSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(v.getContext());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setTitleColor(Color.TRANSPARENT);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                LinearLayout linear = new LinearLayout(v.getContext());
                linear.setOrientation(LinearLayout.VERTICAL);
                linear.setBackgroundColor(Color.WHITE);
                linear.setPadding(60, 20, 60, 20);


                for (final String i : MyResources.allSports) {
                    CheckBox check = new CheckBox(v.getContext());
                    check.setText(i);
                    //  check.setPadding(10, 0, 0, 5);
                    check.setGravity(Gravity.LEFT);
                    if (sports != null) {
                        System.out.println("Sports is not null");
                        for(String s: sports){
                            System.out.println(s + "this is the checked sports");
                        }

                        if(sports != null){
                            if (sports.contains(i)) {
                                check.setChecked(true);
                                if(!msports.contains(i)) {
                                    msports.add(i);
                                }
                            }
                        }
                    }

                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked == true) {

                                msports.add(i);

                            } else {
                                msports.remove(i);
                            }

                        }
                    });
                    linear.addView(check);
                }

                Button button = new Button(v.getContext());
                button.setText("Submit");
                button.setBackgroundColor(Color.parseColor("#99CC00"));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        sports = msports;
                        for(String x: sports){
                            System.out.println(x);
                        }
                        dialog.dismiss();
                    }
                });
                linear.addView(button);
                dialog.setContentView(linear);
                dialog.show();
            }
        });



        updateNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userNew){
                    // Create and Sign in Parse User
                    System.out.println("Creating new user");
                    user = new ParseUser();
                    user.setUsername(email);
                    user.setPassword("zztop");
                    user.setEmail(email);
                }
                try {
                    if (sports != null) {
                        if (sports.size() > 0 && city != null) {
                            String gender = "Female";
                            if (male.isChecked()) {
                                user.put("gender", "Male");
                                gender = "Male";
                            } else {
                                user.put("gender", "Female");
                            }


                            user.put("location", geo);
                            for (String i : sports) {
                                System.out.println(i);
                            }
                            user.put("sports", sports);
                            user.put("city", city);
                            user.put("state", state);

                            MyResources.editUserCity = "";
                            _addressText.setText("");

                            if (!MyResources.editing) {
                                CreateRanks(gender, name, sports);
                                System.out.println("new user");
                            } else {
                                checkSport(gender, user.getString("name"));
                                System.out.println("Returning user");
                                user.saveInBackground();
                                finish();
                            }


                            signUserIn();




                        } else {
                            Toast.makeText(v.getContext(), "Please make sure all required fields are filled out", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "An error occured", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }


    public void signUserIn(){
        final Intent main = new Intent(this, MainActivity.class);
        final Intent load = new Intent(this, LoadingScreen.class);

        if (!MyResources.editing) {
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException o) {
                    System.out.println("Sign up callback dude");
                    if (o == null){
                        user.put("name", name);
                        user.put("profilePicture", photoUrl);
                        user.saveInBackground();
                        for(final ParseObject r : rankingArray){
                            final int rIndex = rankingArray.indexOf(r);

                            r.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        System.out.println("Done");
                                        if (rankingArray.get(rankingArray.size() - 1).equals(rankingArray.get(rIndex))) {
                                            System.out.println("Done saving ranks");
                                            Toast.makeText(NewUserSignUp.this, "Your profile has been saved.", Toast.LENGTH_LONG).show();
                                            startActivity(main);
                                            finish();
                                        }
                                    } else {
                                        e.printStackTrace();
                                        System.out.println("there was a problem");
                                    }
                                }

                            });
                        }

                    } else {
                        System.out.println("There was a problem");
                        o.printStackTrace();
                    }
                }
            });

        } else {
            try {
                user.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MyResources.editing = false;
            startActivity(main);
            finish();
        }
    }

    public void getAddress(Double lat, Double lon) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lon, 30);
            Address address = addressList.get(0);
            if (address != null) {
                StringBuilder deviceAddress = new StringBuilder();
                for (int i = 1; i < address.getMaxAddressLineIndex(); i++) {
                    deviceAddress.append(address.getAddressLine(i))
                            .append(",");
                    if (i == 1) {
                        String tempAddress = address.getAddressLine(1);
                        city = tempAddress;
                        state = tempAddress.substring(tempAddress.length() - 9, tempAddress.length() - 6);

                        MyResources.editUserCity = city;
                        MyResources.changedAddres = true;
                        MyResources.geoAddress = geo;
                    }
                }
                deviceAddress.deleteCharAt(deviceAddress.length() - 1);
                _addressText.setText(deviceAddress.toString());
                Toast.makeText(this, "Successfully updated location", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unable to determine address", Toast.LENGTH_LONG).show();
                //  mLatitudeText.setText("Unable to determine address");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        geo = new ParseGeoPoint();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Double lat = mLastLocation.getLatitude();
            Double lon = mLastLocation.getLongitude();
            geo.setLatitude(lat);
            geo.setLongitude(lon);
            getAddress(lat, lon);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            System.out.println("Exception while starting resolution activity");
            e.printStackTrace();
            retryConnecting();
        }
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void CreateRanks(String gender, String name, final List<String> checkedSports) {
        //Create row for each sport user plays
        System.out.println("Checking ranks");
        System.out.println(checkedSports.size());
        final int sportsLast = checkedSports.size() - 1;

        for (String i : checkedSports) {
            System.out.println(city + state + gender + name);
            ParseObject ranking = new ParseObject("ranks");
            ranking.put("Sport", i);
            ranking.put("Person", user);
            ranking.put("Score", 0);
            ranking.put("City", city.substring(0 , city.length() - 10));
            ranking.put("State", state);
            ranking.put("Gender", gender);
            ranking.put("Name", name);
            final String tempI = i;
            rankingArray.add(ranking);
//            ranking.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e == null){
//                        System.out.println("Done");
//                        if(checkedSports.get(sportsLast).equals(tempI)) {
//                            System.out.println("Done saving ranks");
//
//                        }
//                    } else {
//                        System.out.println("There is a problem");
//                        System.out.println(e.getMessage());
//                    }
//                }
//            });
        }
    }

    private void setGender(String gender){
        if (gender.equals("Male")){
            male.setChecked(true);
        } else {
            female.setChecked(true);
        }
    }

    private void checkSport(final String gender, final String name) {
        ParseQuery<ParseObject> rank = ParseQuery.getQuery("ranks");
        rank.include("Person");
        System.out.println("Checking them sports like a good boy");
        rank.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    System.out.println("Starting ranking for returning user");
                    List<String> checkSports = new ArrayList<String>();
                    checkSports = sports;
                    for (ParseObject i : parseObjects) {
                        ParseUser x = i.getParseUser("Person");
                        if (x.getObjectId().equals(user.getObjectId())) {
                            String rankedSport = i.getString("Sport");

                            System.out.println(rankedSport);
                            if (checkSports.contains(rankedSport)) {
                                checkSports.remove(rankedSport);
                            }
                        }
                    }
                    if (checkSports.size() > 0) {
                        System.out.println("Checked sports is " + checkSports.size());
                        CreateRanks(gender, name, checkSports);
                        for(ParseObject p : rankingArray){
                            p.saveInBackground();
                        }
                    } else {
                        System.out.println("");
                        //   signUserIn();
                    }
                }
            }
        });
    }
}
