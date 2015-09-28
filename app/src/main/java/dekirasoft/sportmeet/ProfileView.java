package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
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
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class ProfileView extends Activity implements AdapterView.OnItemClickListener{

    ParseUser user;
    ParseUser userPro;
    Display display;
    int disWidth, disHeight;
    TextView locationText, invite;
    String userId;
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    LinearLayout ratingLinear;
    private TypedArray navMenuIcons;
    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.profile_view);

        try {
            Intent i = getIntent();
           userId = i.getStringExtra("UserId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        ImageView navi = (ImageView)findViewById(R.id.navi);
        ratingLinear = (LinearLayout)findViewById(R.id.ratingLinear);
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

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            userPro = query.get(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String gender = userPro.getString("gender");
        String profileName = userPro.getString("name");
        String city = userPro.getString("city");
        String imageUrl = userPro.getString("profilePicture");
        List<String> sports = userPro.getList("sports");

        final Intent startInvite = new Intent(this, SendInvite.class);
        invite = (TextView)findViewById(R.id.invite);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startInvite.putExtra("UserId", userId);
                startActivity(startInvite);
            }
        });

        TextView genderProfile = (TextView)findViewById(R.id.userProfileGender);
        genderProfile.setText(gender);
        locationText = (TextView)findViewById(R.id.userProfileLocation);
//        locationText.setText(city);
        locationText.setText(city.substring(0, city.length() - 6));

        TextView nameText = (TextView)findViewById(R.id.userProfileName);
        nameText.setText(profileName);
        TextView sportsPlayed = (TextView)findViewById(R.id.userProfileSports);

        for(String x : sports){
            if(sports.indexOf(x) == sports.lastIndexOf(sports)){
                sportsPlayed.setText(sportsPlayed.getText() + x);
            } else {
                sportsPlayed.setText(sportsPlayed.getText() + x + ", ");
            }
        }

        ImageView image = (ImageView)findViewById(R.id.userProfileImage);

        try {
            setProfilePhoto(imageUrl, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getRatings();
    }

    private void setProfilePhoto(String imageUrl, ImageView profilePicture) throws IOException {

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    private void getRatings(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Ratings");
        query.include("Person");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null){
                    System.out.println("ratings" + parseObjects.size());
                    for(ParseObject i: parseObjects){
                        if(i.getParseUser("Person").getObjectId().equals(userId)){
                            createRatingBar(i);
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void selectItem(int position){
        String title  = navMenuTitles[position];
        if(title.equals("Rankings")){
            Intent i = new Intent(this, RankTables.class);
            startActivity(i);
        } else if(title.equals("Invites")){
            Intent i = new Intent(this, UserInvites.class);
            startActivity(i);
        } else if(title.equals("Profile")){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else if(title.equals("Logout")){
            Intent i = new Intent(this, GooglePlayServicesActivity.class);
            ParseUser.logOut();
            finish();
            startActivity(i);
        }else {
            Intent in = new Intent(this, Search.class);
            startActivity(in);
        }
     }
    private void createRatingBar(ParseObject i){
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
    public void onBackPressed()
    {
        if(mDrawerLayout.isDrawerOpen(mDrawerList)){
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            finish();
        }
    }
}
