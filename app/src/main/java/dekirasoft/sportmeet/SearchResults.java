package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kota on 1/7/2015.
 */
public class SearchResults extends Activity implements AdapterView.OnItemClickListener {

    List<String> playerNames;
    LinearLayout linear;
    Display display;
    int disWidth, disHeight;
    String sports, nameSearch, gender, userId;
    TextView noresults;
    ParseUser currentUser;
    ParseGeoPoint currentGeo;
    int distanceTo;
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TypedArray navMenuIcons;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.search_results);

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        ImageView navi = (ImageView) findViewById(R.id.navi);
        noresults = (TextView) findViewById(R.id.noresults);

        //Get current users id
        userId = ParseUser.getCurrentUser().getObjectId();

        //set adapter for drawerlist
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navMenuTitles));
        //set the lists's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        playerNames = new ArrayList<String>();
        currentGeo = new ParseGeoPoint(0, 0);
        currentUser = ParseUser.getCurrentUser();
        linear = (LinearLayout) findViewById(R.id.searchLinear);
        try {
            Intent intent = getIntent();
            nameSearch = intent.getStringExtra("Name");
            System.out.println(nameSearch);

            sports = MyResources.sportInSearch;
            System.out.println(sports);
            distanceTo = intent.getIntExtra("Distance", 0);
            gender = intent.getStringExtra("Gender");
            currentGeo.setLongitude(intent.getDoubleExtra("Long", 0));
            currentGeo.setLatitude(intent.getDoubleExtra("Lati", 0));

        } catch (Exception e) {
            e.printStackTrace();
        }

        //if user contains only sports
        if (nameSearch == "" && sports != "" && gender == "") {
            final ParseQuery<ParseUser> query = ParseUser.getQuery()
                    .whereEqualTo("sports", sports)
                    .whereWithinMiles("location", currentGeo, distanceTo);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    if (e == null) {
                        //do stuff
                        if (parseUsers.size() > 1) noresults.setText("");
                        System.out.println(parseUsers.size());
                        for (ParseUser i : parseUsers) {
                            if (!i.getObjectId().equals(userId)) {
                                IparseQuery(i);
                            }
                        }
                    } else {
                        //catch
                    }
                }
            });
        }

        //if user contains name only
        if (nameSearch != "" && gender == "") {
            final ParseQuery<ParseUser> query = ParseUser.getQuery()
                    .whereEqualTo("sports", sports)
                    .whereContains("name", nameSearch)
                    .whereWithinMiles("location", currentGeo, distanceTo);
            query.whereNotEqualTo("objectId", userId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    if (e == null) {
                        //do stuff
                        if (parseUsers.size() > 1) noresults.setText("");
                        System.out.println(parseUsers.size());
                        for (ParseUser i : parseUsers) {
                            IparseQuery(i);
                        }
                    } else {
                        //catch
                    }
                }
            });
        }

        //if user contains gender only
        if (nameSearch == "" && gender != "") {
            final ParseQuery<ParseUser> query = ParseUser.getQuery()
                    .whereEqualTo("sports", sports)
                    .whereContains("gender", gender)
                    .whereWithinMiles("location", currentGeo, distanceTo);
            query.whereNotEqualTo("objectId", userId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    if (e == null) {
                        if (parseUsers.size() > 1) noresults.setText("");
                        System.out.println(parseUsers.size());
                        //do stuff
                        for (ParseUser i : parseUsers) {
                            IparseQuery(i);
                        }
                    } else {
                        //catch
                    }
                }
            });
        }

        //If user contains gender and name
        if (nameSearch != "" && gender != "") {
            final ParseQuery<ParseUser> query = ParseUser.getQuery()
                    .whereEqualTo("sports", sports)
                    .whereContains("gender", gender)
                    .whereContains("name", nameSearch)
                    .whereWithinMiles("location", currentGeo, distanceTo);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    if (e == null) {
                        if (parseUsers.size() > 1) noresults.setText("");
                        System.out.println(parseUsers.size());
                        //do stuff
                        for (ParseUser i : parseUsers) {
                            if (!i.getObjectId().equals(userId)) {
                                IparseQuery(i);
                            }
                        }
                    } else {
                        //catch
                    }
                }
            });
        }
    }

    private void IparseQuery(final ParseUser i) {
        try {
            String name = i.getString("name");
            String imageUrl = i.getString("profilePicture");
            String gender = i.getString("gender");
            String city = i.getString("city");
            List<String> iSports = i.getList("sports");

            //Layout Parameters

            //TextView
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Horizontal Layout
            LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Line layout
            LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);

            //Vertical Layout
            LinearLayout.LayoutParams parama = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parama.setMargins(20, 0, 0, 0);

            //Layout Parameters end

            //Green Line
            LinearLayout line = new LinearLayout(this);
            line.setLayoutParams(para);
            line.setBackgroundColor(Color.parseColor("#c0c0c0"));

            ImageView photo = new ImageView(this);
            photo.setPadding(15,0,0,0);

            //Get and set profile picture
            try {
                setProfilePhoto(imageUrl, photo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final Intent prof = new Intent(this, ProfileView.class);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prof.putExtra("UserId", i.getObjectId());
                    startActivity(prof);
                }
            });

            //Main Layout for image misc information
            LinearLayout hor = new LinearLayout(this);
            hor.setOrientation(LinearLayout.HORIZONTAL);
            hor.setLayoutParams(par);
            hor.addView(photo);

            //Add Vertical Layout to horizontal layout for misc information
            LinearLayout vert = new LinearLayout(this);
            vert.setOrientation(LinearLayout.VERTICAL);
            vert.setLayoutParams(parama);

            //Sports Played
            TextView textSport = new TextView(this);
            for (String x : iSports) {
                if (x == iSports.get(iSports.size() - 1)) {
                    textSport.setText(textSport.getText() + x);
                } else
                    textSport.setText(textSport.getText() + x + ", ");
            }
            textSport.setLayoutParams(param);
            //City
            TextView cityLocation = new TextView(this);
            cityLocation.setText(city.substring(0, city.length() - 6));
            cityLocation.setLayoutParams(param);
            //Name
            TextView nameText = new TextView(this);
            nameText.setText(name);
            nameText.setLayoutParams(param);
            //Gender
            TextView genderText = new TextView(this);
            genderText.setText(gender);
            genderText.setLayoutParams(param);
            //Add to Vert
            vert.addView(nameText);
            vert.addView(genderText);
            vert.addView(cityLocation);
            vert.addView(textSport);
            hor.addView(vert);

            //Add in all views into linear
            linear.addView(line);
            linear.addView(hor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProfilePhoto(String imageUrl, ImageView profilePicture) throws IOException {
        profilePicture.setMinimumHeight(100);
        profilePicture.setMinimumWidth(100);
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.android_icon)
                .showImageOnFail(R.drawable.android_icon)
                .showImageOnLoading(R.drawable.android_icon).build();
        imageLoader.displayImage(imageUrl, profilePicture, options);
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
