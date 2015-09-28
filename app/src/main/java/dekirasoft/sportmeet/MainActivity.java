package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    List<String> sports;
    String gender, profileName, city, imageUrl, userId;
    ParseUser userPro;
    LinearLayout ratingLinear;
    ImageView profilePicture, navi;
    TextView locationText, genderProfile, nameText, sportsPlayed, editUser;
    ParseUser user = ParseUser.getCurrentUser();
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        navi = (ImageView) findViewById(R.id.navi);
        ratingLinear = (LinearLayout) findViewById(R.id.ratingLinear);

        //set adapter for drawerlist
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navMenuTitles));
        //set the lists's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        //Get current Useres Id from Parse
        userId = user.getObjectId();

        //Define Xml
        locationText = (TextView) findViewById(R.id.userProfileLocation);
        genderProfile = (TextView) findViewById(R.id.userProfileGender);
        nameText = (TextView) findViewById(R.id.userProfileName);
        sportsPlayed = (TextView) findViewById(R.id.userProfileSports);
        profilePicture = (ImageView) findViewById(R.id.userProfileImage);
        editUser = (TextView) findViewById(R.id.editUser);

        final Intent i = new Intent(this, NewUserSignUp.class);
        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyResources.editing = true;
                startActivity(i);
            }
        });
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        //Subscribe user to its own broadcast
        ParsePush.subscribeInBackground("user_" + userId, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

        //Call Parse Queries
        final ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    System.out.println("Query Successful");
                    try {
                        userPro = query.get(userId);
                        fetchUserInfo();
                        setTextViews();
                    } catch (ParseException exc) {
                        exc.printStackTrace();
                    }
                } else {
                    //failed
                }
            }
        });

        //Get ratings
        getRatings();

    }

    private void fetchUserInfo() {

        Intent i = new Intent(this, MainActivity.class);
        gender = userPro.getString("gender");
        profileName = userPro.getString("name");
        city = userPro.getString("city");
        imageUrl = userPro.getString("profilePicture");
        System.out.println(gender + profileName + city + imageUrl);
        sports = userPro.getList("sports");
        MyResources.currentUserSports = sports;
        MyResources.currentUserName = profileName;
        MyResources.mcity = city;
        if (city == null) {
            startActivity(i);
        }
    }

    private void setTextViews() {
        genderProfile.setText(gender);
        locationText.setText(city.substring(0, city.length() - 6));
        nameText.setText(profileName);
        String sportText = "";
        for (String x : sports) {
            if (sports.indexOf(x) == sports.size() + 1) {
                sportText += x;
            } else {
                sportText += x + ", ";
            }
        }
        sportsPlayed.setText(sportText);
        try {
            setProfilePhoto();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // getInvites();
    }

    private void setProfilePhoto() throws IOException {

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

    private void getRatings() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Ratings");
        query.include("Person");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    System.out.println(parseObjects.size());
                    for (ParseObject i : parseObjects) {
                        if (i.getParseUser("Person").getObjectId().equals(userId)) {
                            createRatingBar(i);
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createRatingBar(ParseObject i) {
        int ratingNum = i.getInt("Rating");
        String comment = i.getString("Comment");
        String name = i.getString("Name");
        Date date = i.getCreatedAt();
        String dateOf = date.toString().substring(4, 10) + ", " + date.toString().substring(24, 28);
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setRating(ratingNum);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 3, 10, 3);
        ratingBar.setLayoutParams(layoutParams);

        //Name and date text
        TextView nameDate = new TextView(this);
        nameDate.setText(comment + "\n" + "by " + name + " on " + dateOf);

        LinearLayout hori = new LinearLayout(this);
        hori.setOrientation(LinearLayout.HORIZONTAL);

        hori.addView(ratingBar);
        hori.addView(nameDate);
        ratingLinear.addView(hori);
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


