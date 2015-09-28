package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by Kota on 1/7/2015.
 */
public class Search extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {
    String gender, userCity;
    double latitude, longitude;
    SeekBar dis;
    RadioButton male, female, both;
    ImageView previous, next, search, navi;
    TextView distanceText;
    EditText zipSearch, name;
    Button selectSports, done;
    int distantTo = 25;
    Geocoder geo;
    TextView cityText, currentCity, citySearch;
    ViewSwitcher switcher;
    List<Address> addresses;
    ParseUser user;
    ViewFlipper viewSwitcher2;
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TypedArray navMenuIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        //Define all xml elements
        navi = (ImageView) findViewById(R.id.navi);
        previous = (ImageView) findViewById(R.id.previous);
        next = (ImageView) findViewById(R.id.next);
        search = (ImageView) findViewById(R.id.search);
        name = (EditText) findViewById(R.id.searchName);
        selectSports = (Button) findViewById(R.id.searchBySport);
        currentCity = (TextView) findViewById(R.id.userCurrentCity);
        switcher = (ViewSwitcher) findViewById(R.id.viewSwitch);
        cityText = (TextView) findViewById(R.id.searchLocationCity);
        dis = (SeekBar) findViewById(R.id.seekDistance);
        distanceText = (TextView) findViewById(R.id.distanceText);
        gender = "";
        zipSearch = (EditText) findViewById(R.id.zipSearch);
        viewSwitcher2 = (ViewFlipper) findViewById(R.id.viewSwitch2);
        //citySearch = (TextView)findViewById(R.id.citySearch);
        geo = new Geocoder(this);
        user = ParseUser.getCurrentUser();
        Intent intent = null;

        try {
            intent = Intent.getIntent("intent");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        userCity = intent.getStringExtra("userCity");
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //set adapter for drawerlist
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navMenuTitles));
        //set the lists's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        //Radio Group
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        both = (RadioButton) findViewById(R.id.both);

        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        //hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(
                this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(name.getWindowToken(), 0);

        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        search.setOnClickListener(this);
        selectSports.setOnClickListener(this);

        //seekbar
        dis.setProgress(0);
        dis.incrementProgressBy(1);
        dis.setMax(13);
        distanceText.setTextColor(Color.BLACK);
        distanceText.setTextSize(18);
        distanceText.setText("25 Miles");
        distanceText.setGravity(Gravity.CENTER);
        dis.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == next.getId()) {
            viewSwitcher2.showNext();
        } else if (v.getId() == previous.getId()) {
            viewSwitcher2.showPrevious();
        } else if (v.getId() == search.getId()) {
            startSearch();
        } else if (v.getId() == selectSports.getId()) {
            final Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setTitleColor(Color.TRANSPARENT);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            RadioGroup sportSelect = new RadioGroup(this);
            for (String i : MyResources.currentUserSports) {
                final RadioButton radio = new RadioButton(this);
                radio.setText(i);
               // radio.setPadding(10, 0, 0, 15);
                radio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyResources.sportInSearch = radio.getText().toString();
                    }
                });
                sportSelect.addView(radio);
                if (i == MyResources.sportInSearch) {
                    radio.setChecked(true);
                }
            }

            done = new Button(this);
            done.setBackgroundColor(Color.parseColor("#99CC00"));
            done.setText("Done");
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            LinearLayout linear = new LinearLayout(this);
            linear.setOrientation(LinearLayout.VERTICAL);
            linear.setPadding(60, 20, 60, 20);
            linear.addView(sportSelect);
            linear.addView(done);
            linear.setBackgroundColor(Color.WHITE);
            dialog.setContentView(linear);
            dialog.show();
        }
    }

    private void RadioButtonClick(RadioButton radioButton) {
        RadioButton rb = radioButton;
        MyResources.sportInSearch = rb.getText().toString();
    }

    public void startSearch() {
        if (MyResources.sportInSearch != "") {
            if (male.isChecked()) {
                gender = "Male";
            } else if (female.isChecked()) {
                gender = "Female";
            } else {
                gender = "";
            }

            final Intent searchCrit = new Intent(this, SearchResults.class);

            //zip code
            if (zipSearch == viewSwitcher2.getCurrentView()) {

                if (zipSearch.getText().toString() != "" && zipSearch.getText().length() == 5) {
                    try {
                        addresses = geo.getFromLocationName(zipSearch.getText().toString(), 1);
                        Address zipAddress = addresses.get(0);
                        latitude = zipAddress.getLatitude();
                        longitude = zipAddress.getLongitude();
                    } catch (IOException e) {
                        Toast.makeText(this, "There was an error with your zip code. Please try again" +
                                ", or use a different method", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }

                } else {
                    Toast.makeText(this, "Please use 5 digit zip code", Toast.LENGTH_LONG).show();
                    return;
                }

            //city search
            } else if (citySearch == viewSwitcher2.getCurrentView()) {
                if (citySearch.getText().toString() != "") {
                    try {
                        addresses = geo.getFromLocationName(citySearch.getText().toString(), 1);
                        Address cityAddress = addresses.get(0);
                        latitude = cityAddress.getLatitude();
                        longitude = cityAddress.getLongitude();
                    } catch (IOException e) {
                        Toast.makeText(this, "There was an error with the city you entered" +
                                ". Please try again, or use a different method", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }
                } else {
                    Toast.makeText(this, "Please make sure you enter a valid city", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (currentCity == viewSwitcher2.getCurrentView()) {
                try {
                    ParseGeoPoint g = user.getParseGeoPoint("location");
                    latitude = g.getLatitude();
                    longitude = g.getLongitude();
                } catch (Exception e) {
                    Toast.makeText(this, "There was an error. Please try again", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    return;
                }
            }

            searchCrit.putExtra("Name", name.getText().toString());
            searchCrit.putExtra("Distance", distantTo);
            searchCrit.putExtra("Gender", gender);
            searchCrit.putExtra("Lati", latitude);
            searchCrit.putExtra("Long", longitude);
            startActivity(searchCrit);
        } else {
            Toast.makeText(this, "Please select a sport.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb = (RadioButton) findViewById(group.getCheckedRadioButtonId());
        RadioButtonClick(rb);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int[] distantArray = {5, 10, 20, 25, 50, 100, 150, 200, 300, 500, 1000, 2000, 3000, 5000};
        int arrayCount = seekBar.getProgress();
        distanceText.setText(distantArray[arrayCount] + " Miles");
        distantTo = distantArray[arrayCount];
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, navMenuTitles[position].toString(), Toast.LENGTH_LONG).show();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        String title = navMenuTitles[position];
        if (title.equals("Rankings")) {
            Intent i = new Intent(this, RankTables.class);
            startActivity(i);
        } else if (title.equals("Invites")) {
            Intent i = new Intent(this, UserInvites.class);
            startActivity(i);
        } else if (title.equals("Profile")) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else if (title.equals("Logout")) {
            Intent i = new Intent(this, GooglePlayServicesActivity.class);
            ParseUser.logOut();
            finish();
            startActivity(i);
        } else {
            Intent in = new Intent(this, Search.class);
            startActivity(in);
        }

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            finish();
        }
    }
}
