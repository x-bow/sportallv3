package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
public class UserInvites extends Activity implements AdapterView.OnItemClickListener {

    LinearLayout pending, accepted, declined, sent;
    ImageView navi;
    ScrollView scroll;
    ParseUser user = ParseUser.getCurrentUser();
    Display display;
    int disWidth, disHeight;
    private String[] navMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TypedArray navMenuIcons;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.user_invites);
        if (ParseUser.getCurrentUser() == null) {
            finish();
        }
        //Define xml elements
        scroll = (ScrollView) findViewById(R.id.scroll);
        pending = (LinearLayout) findViewById(R.id.pending);
        accepted = (LinearLayout) findViewById(R.id.accepted);
        declined = (LinearLayout) findViewById(R.id.declined);
        sent = (LinearLayout) findViewById(R.id.sent);
        navi = (ImageView) findViewById(R.id.navi);

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //set adapter for drawerlist
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navMenuTitles));
        //set the lists's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getInvites(user);
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });
    }

    private void getInvites(ParseUser user) {
        try {
            ParseQuery<ParseObject> rquery = ParseQuery.getQuery("Invite")
                    .whereEqualTo("userInvited", user)
                    .orderByDescending("createdAt");
            rquery.include("userCreated");
            if (rquery != null) {
                rquery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            //success
                            for (ParseObject i : parseObjects) {
                                try {
                                    IParseQuery(i);
                                } catch (NullPointerException eexc) {
                                    eexc.printStackTrace();
                                }
                            }
                        } else {
                            e.printStackTrace();
                            //failed
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Invite")
                .whereEqualTo("userCreated", user)
                .orderByDescending("createdAt");
        query.include("userInvited");
        query.include("userCreated");
        if (query != null) {
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        for (ParseObject i : parseObjects) {
                            try {
                                SentQuery(i);
                            } catch (NullPointerException excee) {
                                System.out.println(excee.getMessage());
                                excee.printStackTrace();
                            }
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void SentQuery(final ParseObject i) {
        try {
            final String channel = i.getString("channel");
            String requestType = "Pending";
            boolean showWon = false;
            //Retrieve all queries
            final String dateOf = i.getString("dateOf");
            final String location = i.getString("location");
            boolean request = false;
            if (i.containsKey("request")) {
                request = i.getBoolean("request");
                if (request == false) {
                    requestType = "Declined";
                } else {
                    requestType = "Accepted";
                    showWon = true;
                }
            }
            final String sport = i.getString("sport");
            ParseUser userInvited = i.getParseUser("userInvited");
            String userId = userInvited.getObjectId();
            final String time = i.getString("time");
            String imageUrl = userInvited.getString("profilePicture");
            String name = userInvited.getString("name");
            String gender = userInvited.getString("gender");
            String locationUser = userInvited.getString("city");
            List<String> sportsUser = userInvited.getList("sports");
            String sportText = "";

            for (String x : sportsUser) {
                if (sportsUser.indexOf(x) == sportsUser.size()) {
                    sportText += x;
                } else {
                    sportText += x + ", ";
                }
            }
            List<HashMap<String, String>> comments = new ArrayList<HashMap<String, String>>();
            if (i.containsKey("comments")) {
                comments = i.getList("comments");
            }

            //TextView
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //Horizontal layout
            LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            //Line layout
            LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);

            //Vertical Layout
            LinearLayout.LayoutParams parama = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            parama.setMargins(0, 20, 0, 0);

            //Layout Parameters End

            LinearLayout line = new LinearLayout(this);
            line.setLayoutParams(para);
            line.setBackgroundColor(Color.parseColor("#c0c0c0"));

            //Get and set Profile Picture
            ImageView photo = new ImageView(this);
            photo.setPadding(15,0,15,0);
            photo.setLayoutParams(par);

            setProfilePhoto(imageUrl, photo);

            //Main layout for image misc information
            LinearLayout hor = new LinearLayout(this);
            hor.setOrientation(LinearLayout.HORIZONTAL);
            hor.setLayoutParams(par);
            hor.addView(photo);

            //Add Vertical Layout to horizontal layout for misc information
            LinearLayout vert = new LinearLayout(this);
            vert.setOrientation(LinearLayout.VERTICAL);
            vert.setLayoutParams(parama);

            TextView Irequest = new TextView(this);

//            Irequest.setText(requestType + "\n" + name + "\n" + gender + "\n" + locationUser + "\n" + sportText);
            Irequest.setText(requestType + "\n" + name + "\n" + "Sport: " + sport + "\n" +"Location: " + location);
            Irequest.setGravity(View.TEXT_ALIGNMENT_CENTER);
            hor.addView(Irequest);

            sent.addView(hor);
            sent.addView(line);
            final String requestType2 = "Sent";
            //send intent if horizontal values are clicked
            final Intent intent = new Intent(this, ViewInvite.class);
            final boolean finalShowWon = showWon;
            final boolean finalShowWon1 = showWon;
            hor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent.putExtra("location", location);
                    intent.putExtra("time", time);
                    intent.putExtra("dateOf", dateOf);
                    intent.putExtra("sport", sport);
                    intent.putExtra("showWon", finalShowWon);
                    intent.putExtra("objId", i.getObjectId());
                    intent.putExtra("request", requestType2);
                    intent.putExtra("showWon", finalShowWon1);
                    intent.putExtra("channel", channel);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void IParseQuery(final ParseObject i) {
        String requestType = "Pending";
        //Retrieve all queries
        final String channel = i.getString("channel");
        final String dateOf = i.getString("dateOf");
        final String location = i.getString("location");
        boolean request = false;
        if (i.containsKey("request")) {
            request = i.getBoolean("request");
            if (request == false)
                requestType = "Declined";
            else
                requestType = "Accepted";
        }
        final String sport = i.getString("sport");
        ParseUser userCreated = i.getParseUser("userCreated");
        String userId = userCreated.getObjectId();
        final String time = i.getString("time");
        String imageUrl = userCreated.getString("profilePicture");
        String name = userCreated.getString("name");
        String gender = userCreated.getString("gender");
        String locationUser = userCreated.getString("city");
        List<String> sportsUser = userCreated.getList("sports");
        String sportText = "";

        for (String x : sportsUser) {
            if (sportsUser.indexOf(x) == sportsUser.size()) {
                sportText += x;
            } else {
                sportText += x + ", ";
            }
        }
        List<HashMap<String, String>> comments = new ArrayList<HashMap<String, String>>();
        if (i.containsKey("comments")) {
            comments = i.getList("comments");
        }

        //TextView
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //Horizontal layout
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //Line layout
        LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);

        //Vertical Layout
        LinearLayout.LayoutParams parama = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        parama.setMargins(0, 20, 0, 0);

        //Layout Parameters End

        //Green Line
        LinearLayout line = new LinearLayout(this);
        line.setLayoutParams(para);
        line.setBackgroundColor(Color.parseColor("#c0c0c0"));

        //Get and Set Profile Picture
        ImageView photo = new ImageView(this);
        photo.setPadding(15,0,15,0);
        photo.setLayoutParams(par);
        try {
            setProfilePhoto(imageUrl, photo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Main Layout for Image misc information
        LinearLayout hor = new LinearLayout(this);
        hor.setOrientation(LinearLayout.HORIZONTAL);
        hor.setLayoutParams(par);
        hor.addView(photo);

        //Add Vertical Layout to horizontal layout for misc information
        LinearLayout vert = new LinearLayout(this);
        vert.setOrientation(LinearLayout.VERTICAL);
        vert.setLayoutParams(parama);

        TextView textSport = new TextView(this); //Sports Played
        TextView cityLocation = new TextView(this); //City
        /*if (location.equals("")) {
            cityLocation.setText("Not Specified");
        } else {
            cityLocation.setText(location);
        }*/

        //Add to Vert
//        textSport.setText("Location: " + cityLocation.getText() + "\n" + "Sport: " + sport + "\n" + name + "\n" + gender + "\n" + locationUser + "\n" + sportText);
        textSport.setText(requestType + "\n" + name + "\n" + "Sport: " + sport + "\n" +"Location: " + location);
        textSport.setGravity(View.TEXT_ALIGNMENT_CENTER);
        hor.addView(textSport);
        hor.addView(vert);

        //Add in all views into linear for pending requests
        if (requestType == "Pending") {
            pending.addView(hor);
            pending.addView(line);
        } else if (requestType == "Accepted") {
            accepted.addView(hor);
            accepted.addView(line);
        } else {
//            declined.addView(text);
            declined.addView(hor);
            declined.addView(line);
        }

        final Intent o = new Intent(this, ViewInvite.class);
        //send intent it horizontal values are clicked
        final String finalRequestType = requestType;
        hor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                o.putExtra("location", location);
                o.putExtra("time", time);
                o.putExtra("dateOf", dateOf);
                o.putExtra("sport", sport);
                o.putExtra("objId", i.getObjectId());
                o.putExtra("request", finalRequestType);
                o.putExtra("showWon", true);
                o.putExtra("channel", channel);
                startActivity(o);
            }
        });
        {
        }
        ;
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
