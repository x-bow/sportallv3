package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class RankTables extends Activity implements AdapterView.OnItemClickListener {

    Spinner sportSpinner, citySpinner, stateSpinner, genderSpinner;
    List<String> cities, states, sports, gender, allUsers;

    Button submitSearch;
    String city, state, sport, mgender;
    TableRow tr;
    TableLayout table;
    TableRow.LayoutParams par;
    List<Integer> ids;
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TypedArray navMenuIcons;
    ImageView navi;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.rank_tables);

        sportSpinner = (Spinner) findViewById(R.id.sportSpinner);
        citySpinner = (Spinner) findViewById(R.id.citySpinner);
        stateSpinner = (Spinner) findViewById(R.id.stateSpinner);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        submitSearch = (Button) findViewById(R.id.submitSearch);
        table = (TableLayout) findViewById(R.id.tableLayout1);
        navi = (ImageView) findViewById(R.id.navi);

        //Drawer list
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //set adapter for drawerlist
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navMenuTitles));
        //set the lists's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        ids = new ArrayList<Integer>();
        cities = new ArrayList<String>();
        states = new ArrayList<String>();
        sports = new ArrayList<String>();
        gender = new ArrayList<String>();

        allUsers = new ArrayList<String>();

        CallQueryCities();

        submitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ids.size() > 0) {
                    for (int i : ids) {
                        table.removeViewAt(1);
                    }
                    ids.clear();
                }
                city = citySpinner.getSelectedItem().toString();
                state = stateSpinner.getSelectedItem().toString();
                mgender = genderSpinner.getSelectedItem().toString();
                sport = sportSpinner.getSelectedItem().toString();

                SearchQuery(city, state, mgender, sport);
            }
        });
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });
    }

    private void CallQueryCities() {
        ParseQuery<ParseObject> CityQuery = ParseQuery.getQuery("ranks");

        CityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (ParseObject i : parseObjects) {

                        String state = i.getString("State");

                        if (!states.contains(state)) {
                            states.add(state);
                        }
                    }
                    cities.add("All");
                    AddToCity();
                    AddToState();
                    AddToSport();
                    AddToGender();
                } else {
                    //error collecting
                }
            }
        });
    }

    private void AddToCity() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, cities);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        citySpinner.setAdapter(dataAdapter);
        dataAdapter.notifyDataSetChanged();
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                citySpinner.setPrompt(cities.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                citySpinner.setPrompt("All");
            }
        });
    }

    private void AddToGender() {
        gender.add("Both");
        gender.add("Male");
        gender.add("Female");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, gender);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        genderSpinner.setAdapter(dataAdapter);
    }

    private void AddToState() {
        states.add(0, "All");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, states);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        stateSpinner.setAdapter(dataAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    cities.clear();
                    cities.add("All");
                    AddToCity();
                } else {
                    queryCities(states.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    private void AddToSport() {
        if(MyResources.allSports.size() > 0 && MyResources.allSports != null) {
            for (String i : MyResources.allSports) {
                sports.add(i);
            }
        } else {
            final List<String> sportsNow = new ArrayList<String>();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Sports");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        for (ParseObject i : parseObjects) {
                            String x = i.getString("sport");
                            sportsNow.add(x);
                        }
                        MyResources.allSports = sportsNow;
                        AddToSport();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, sports);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sportSpinner.setAdapter(dataAdapter);
    }

    private void SearchQuery(String city, String state, String gender, final String sport) {

        ParseQuery CityQuery = new ParseQuery("ranks");
        allUsers.clear();
        if (city != "All" && state != "All" && gender == "Both") {

            CityQuery.whereEqualTo("City", city)
                    .whereEqualTo("State", state)
                    .whereEqualTo("Sport", sport)

                    .orderByDescending("Score");
            CityQuery.include("Person");

        } else if (city != "All" && state != "All" && state != "Both") {

            CityQuery.whereEqualTo("City", city)
                    .whereEqualTo("State", state)
                    .whereEqualTo("Sport", sport)
                    .whereEqualTo("Gender", gender)
                    .orderByDescending("Score");
            CityQuery.include("Person");

        } else if (city != "All" && state == "All" && gender == "Both") {

            CityQuery.whereEqualTo("Sport", sport)
                    .whereEqualTo("City", city)
                    .orderByDescending("Score");
            CityQuery.include("Person");
        } else if (city == "All" && state == "All" && gender == "Both") {

            CityQuery.whereEqualTo("Sport", sport)
                    .orderByDescending("Score");
            CityQuery.include("Person");

        } else if (city == "All" && state != "All" && gender == "Both") {
            CityQuery.whereEqualTo("Sport", sport)
                    .whereEqualTo("State", state)
                    .orderByDescending("Score");
            CityQuery.include("Person");
        } else if (city == "All" && state != "All" && gender != "Both") {
            CityQuery
                    .whereEqualTo("State", state)
                    .whereEqualTo("Sport", sport)
                    .whereEqualTo("Gender", gender)
                    .orderByDescending("Score");
            CityQuery.include("Person");
        } else if (city == "All" && state == "All" && gender != "Both") {

            CityQuery
                    .whereEqualTo("Sport", sport)
                    .whereEqualTo("Gender", gender)
                    .orderByDescending("Score");
            CityQuery.include("Person");

        } else {
            System.out.println("Error finding selected items");
            return;
        }

        CityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    int count = 0;
                    System.out.println("Made it to parse objects");
                    int n = parseObjects.size();
                    System.out.println(n);
                    for (ParseObject i : parseObjects) {
                        count++;

                        int score = i.getInt("Score");
                        ParseUser user = i.getParseUser("Person");
                        String name = user.getString("name");
                        String objId = user.getObjectId();
                        if(allUsers.contains(objId)){
                            count--;
                           i.deleteInBackground();
                        } else {
                            allUsers.add(user.getObjectId());
                            System.out.println(name);
                            DrawToTable(name, Integer.toString(count), Integer.toString(score), objId, user);
                        }

                    }
                } else {
                    //Something went wrong. error check
                    e.printStackTrace();
                }
            }
        });
    }

    private void DrawToTable(String name, String points, String rank, final String objId, ParseUser user) {
        par = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        par.setMargins(2, 2, 2, 2);
        tr = new TableRow(this);
        tr.setLayoutParams(par);
        ImageView image = new ImageView(this);
        image.setPadding(0,0,0,5);
        try {
            setProfilePhoto(user.getString("profilePicture"), image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateTextView(name);
        CreateTextView(rank);
        CreateTextView(points);
        table.addView(tr, table.getChildCount(), new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ids.add(table.indexOfChild(tr));

        final Intent i = new Intent(this, ProfileView.class);
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i.putExtra("UserId", objId);
                MyResources.sportInSearch = sportSpinner.getSelectedItem().toString();
                startActivity(i);
            }
        });
    }

    private void CreateTextView(String text) {
        if (text.length() > 20) {
            text = text.substring(0, 18) + "...";
        }
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(par);
        tr.addView(textView);
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

    public void queryCities(String state) {
        ParseQuery<ParseObject> query = new ParseQuery("ranks");
        query.whereEqualTo("State", state);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    cities.clear();
                    cities.add("All");
                    for (ParseObject i : parseObjects) {
                        String city = i.getString("City");
                        if (!cities.contains(city)) {
                            cities.add(city);
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
        AddToCity();
    }

    private void setProfilePhoto(String imageUrl, ImageView profilePicture) throws IOException {

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.android_icon)
                .showImageOnFail(R.drawable.android_icon)
                .showImageOnLoading(R.drawable.android_icon).build();
        imageLoader.displayImage(imageUrl, profilePicture, options);
        tr.addView(profilePicture);
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

