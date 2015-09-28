package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class ReviewInvite extends Activity {
    String location, year, day, time, sportPlaying, userId,
            photoUrl, invitedObject, createdObject, notes;
    ImageView invitePhoto;
    TextView nameUser, locationUser, gender, locationView, timeview, notesView, dayView, yearView, sportView, edit,
            send, sportsText;
    Display display;
    Button sendComment;
    EditText editcomment;
    int disWidth, disHeight, count, invitedRank, createdRank;
    ParseUser userPro;
    LinearLayout commentLinear;
    List<Integer> scores;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.review_invite);

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        disWidth = size.x;
        disHeight = size.y;


//        //Hide Buttons
//        sendComment = (Button)findViewById(R.id.postComment);
//        sendComment.setVisibility(View.GONE);
//        editcomment = (EditText)findViewById(R.id.editComment);
//        editcomment.setVisibility(View.GONE);

        //Retrieve intent
        try {
            Intent in = getIntent();
            location = in.getStringExtra("location");
            time = in.getStringExtra("time");
            notes = in.getStringExtra("notes");
            day = in.getStringExtra("day");
            year = in.getStringExtra("year");
            sportPlaying = in.getStringExtra("sport");
            userId = in.getStringExtra("userId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Define all xml elements
        nameUser = (TextView) findViewById(R.id.userProfileName);
        locationUser = (TextView) findViewById(R.id.userProfileLocation);
        gender = (TextView) findViewById(R.id.userProfileGender);
        locationView = (TextView) findViewById(R.id.locationView);
        dayView = (TextView) findViewById(R.id.dayView);
        yearView = (TextView) findViewById(R.id.yearView);
        sportView = (TextView) findViewById(R.id.sportView);
        sportsText = (TextView) findViewById(R.id.userProfileSports);
        invitePhoto = (ImageView) findViewById(R.id.userProfileImage);
        edit = (TextView) findViewById(R.id.edit);
        send = (TextView) findViewById(R.id.send);
        timeview = (TextView) findViewById(R.id.inviteTime);
        commentLinear = (LinearLayout) findViewById(R.id.commentLinear);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            userPro = query.get(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashMap<String, String> userCommenting = new HashMap<String, String>();
        final List<HashMap<String, String>> commentArray = new ArrayList<HashMap<String, String>>();
        if (!notes.trim().isEmpty()) {
            PopulateComments(notes);
            userCommenting.put("user", ParseUser.getCurrentUser().getObjectId());
            userCommenting.put("comment", notes);
            commentArray.add(userCommenting);
            for (String key : userCommenting.keySet()) {

            }
        }

        photoUrl = userPro.getString("profilePicture");
        try {
            setProfilePhoto(invitePhoto, photoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Populate all views
        if (location != "") {
            locationView.setText(location);
        }
        dayView.setText(day);
//        yearView.setText(year);
        yearView.setText(new StringBuilder().append(",").append(year));

        sportView.setText(sportPlaying);
        nameUser.setText(userPro.getString("name"));
//        locationUser.setText(userPro.getString("city"));
        locationUser.setText(userPro.getString("city").substring(0, userPro.getString("city").length() - 6));
        gender.setText(userPro.getString("gender"));
        timeview.setText(time);

        String sportText = "";
//        List<String> sports = userPro.getList("sports");
//        for (String x : sports) {
//
//                sportText += x + " ";
//
//        }
        sportsText.setText(sportText);


        //clicks
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Call query on rank table to receive rank of users
        ParseQuery<ParseObject> rankQuery = ParseQuery.getQuery("ranks")
                .whereEqualTo("Sport", sportPlaying)
                .orderByAscending("Score");
        rankQuery.include("Person");
        rankQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    //loop through and build up count
                    System.out.println("Parse Objects size" + parseObjects.size());
                    for (ParseObject i : parseObjects) {
                        ParseUser user = i.getParseUser("Person");
                        int score = i.getInt("Score");
                        if (user.getObjectId().equals(userPro.getObjectId())) {
                            invitedRank = count;
                            invitedObject = i.getObjectId();
                            if (score == 0) {
                                invitedRank = 1;
                            }
                            System.out.println(Integer.toString(count));
                        }
                        if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            createdRank = count;
                            if (score == 0) {
                                createdRank = 1;
                            }
                            createdObject = i.getObjectId();
                        }
                        if (invitedRank != 0 && createdRank != 0) {
                            break;
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });

        final Intent main = new Intent(this, MainActivity.class);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ParseUser userI = ParseUser.getCurrentUser();
                    List<String> tempSports = userI.getList("sports");
                    for(String ls: tempSports){
                        System.out.println("Before save" + ls);
                    }
                    ParseObject obji = new ParseObject("Invite");
                    obji.put("dateOf", day + "," + year);
                    obji.put("userCreated", ParseUser.getCurrentUser());
                    obji.put("location", location);
                    if (!notes.trim().isEmpty()) {
                        obji.put("comments", commentArray);
                    }
                    obji.put("time", time);
                    obji.put("sport", sportPlaying);
                    obji.put("userInvited", userPro);
                    obji.put("invitedRank", invitedRank);
                    obji.put("createdRank", createdRank);
                    obji.put("invitedObject", invitedObject);
                    obji.put("createdObject", createdObject);
                    obji.put("userInvitedEmail", userPro.getString("email"));
                    obji.save();

                    //Subscribe user to  broadcast
                    ParsePush.subscribeInBackground("invite_" + obji.getObjectId(), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                            } else {
                                Log.e("com.parse.push", "failed to subscribe for push", e);
                            }
                        }
                    });
                    obji.put("channel", "invite_" + obji.getObjectId());
                    tempSports = userI.getList("sports");
                    for(String ls: tempSports){
                        System.out.println("Before save" + ls);
                    }
                    obji.saveInBackground();
                    if(MyResources.allSports != null){
                        for(String n: MyResources.currentUserSports){
                            System.out.println(n);
                        }

                        userI.put("sports", MyResources.currentUserSports);
                        userI.saveInBackground();
                    }
                    //Send new push
                    JSONObject data = null;
                    try {
                        data = new JSONObject();

                        data.put("title", " \"You Got An Invite To Play");
                        data.put("alert", sportPlaying + " from " + MyResources.currentUserName);

                        data.put("customdata", "My String");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ParsePush parsePush = new ParsePush();
                    parsePush.setData(data);
                    parsePush.setChannel("user_" + userId);
                    parsePush.sendInBackground();

                    startActivity(main);
                    Toast.makeText(v.getContext(), "Your invitation has been sent.", Toast.LENGTH_LONG).show();
                    finish();
                } catch
                        (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setProfilePhoto(ImageView profilePicture, String imageUrl) throws IOException {

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.android_icon)
                .showImageOnFail(R.drawable.android_icon)
                .showImageOnLoading(R.drawable.android_icon).build();
        imageLoader.displayImage(imageUrl, profilePicture, options);
    }

    private void PopulateComments(String comment) {
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        String name = MyResources.currentUserName;
        //Create TextViews
        TextView text = new TextView(this);
        text.setText(name + ": " + comment);
        text.setLayoutParams(par);

        //Add to view
        commentLinear.addView(text);
    }
}
