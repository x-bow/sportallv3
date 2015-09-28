package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class ViewInvite extends Activity {

    String location, time, dateOf, sport, objId, map, request, winningPlayer,
            invitedObject, createdObject, finished, channel;
    List<HashMap<String, String>> comments;
    TextView decline, accept, nameUser, locationUser, dayView, yearOf, locationView, sportView, timeView, genderUser,
            sportsText, selectWinner;
    ImageView invitePhoto;
    Display display;
    int disWidth, disHeight, invitedRank, createdRank;
    LinearLayout commentLinear;
    Button sendComment;
    EditText editComment;
    ParseObject invite, rank;
    RadioButton playerOne, playerTwo;
    RadioGroup radioGroup;
    boolean showWon;
    RatingBar ratingBar;
    ParseUser userCreated, userInvited;
    Boolean showRating = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.invite);

        selectWinner = (TextView) findViewById(R.id.selectWinner);
        playerOne = (RadioButton) findViewById(R.id.playerOne);
        playerTwo = (RadioButton) findViewById(R.id.playerTwo);
        genderUser = (TextView) findViewById(R.id.userProfileGender);
        sportsText = (TextView) findViewById(R.id.sportsText);
        decline = (TextView) findViewById(R.id.edit);
        accept = (TextView) findViewById(R.id.send);
        nameUser = (TextView) findViewById(R.id.nameUser);
        locationUser = (TextView) findViewById(R.id.locationUser);
        dayView = (TextView) findViewById(R.id.dayView);
        yearOf = (TextView) findViewById(R.id.yearView);
        locationView = (TextView) findViewById(R.id.locationView);
        sportView = (TextView) findViewById(R.id.sportView);
        timeView = (TextView) findViewById(R.id.inviteTime);
        commentLinear = (LinearLayout) findViewById(R.id.commentLinear);
        sendComment = (Button) findViewById(R.id.postComment);
        editComment = (EditText) findViewById(R.id.editComment);
        invitePhoto = (ImageView) findViewById(R.id.invitePhoto);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);

        decline.setText("Decline");
        accept.setText("Accept");
        decline.setVisibility(View.INVISIBLE);
        accept.setVisibility(View.INVISIBLE);
        radioGroup.setVisibility(View.GONE);
        selectWinner.setVisibility(View.GONE);

        try {
            Intent in = getIntent();
            location = in.getStringExtra("location");
            time = in.getStringExtra("time");
            dateOf = in.getStringExtra("dateOf");
            sport = in.getStringExtra("sport");
            objId = in.getStringExtra("objId");
            request = in.getStringExtra("request");
            showWon = in.getBooleanExtra("showWon", false);
            channel = in.getStringExtra("channel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // display radio buttons if accepted
        if (request.equals("Accepted")) {
            accept.setText("Submit");
            accept.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.VISIBLE);
            selectWinner.setVisibility(View.VISIBLE);
        } else if (request.equals("Sent")) {
            if (showWon == true) {
                accept.setText("Submit");
                accept.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);
                selectWinner.setVisibility(View.VISIBLE);
            }
        } else {
        }

        //Populate View
        locationView.setText(location);
        dayView.setText(dateOf);
        sportView.setText(sport);
        timeView.setText(time);
        comments = new ArrayList<HashMap<String, String>>();

        //Call query to object
        ParseQuery<ParseObject> Iquery = ParseQuery.getQuery("Invite");
        Iquery.include("userCreated");
        Iquery.include("userInvited");
        try {
            invite = Iquery.get(objId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        userCreated = invite.getParseUser("userCreated");
        userInvited = invite.getParseUser("userInvited");
        invitedRank = invite.getInt("invitedRank");
        createdRank = invite.getInt("createdRank");
        invitedObject = invite.getString("invitedObject");
        createdObject = invite.getString("createdObject");

        if (invite.containsKey("finished")) {
            finished = invite.getString("finished");
            winningPlayer = invite.getString("whoWon");
            if (finished.contains(ParseUser.getCurrentUser().getObjectId().toString())) {
                accept.setVisibility(View.INVISIBLE);
                radioGroup.setVisibility(View.GONE);
                setWinner(winningPlayer);
            } else {
                showRating = true;
            }
        } else {
            winningPlayer = "";
            finished = "";
            showRating = true;
        }

        PopulateUserTopCreated(invite);

        if (invite.containsKey("comments")) {
            comments = invite.getList("comments");
            for (HashMap<String, String> d : comments) {
                PopulateComments(d);
            }
        }

        //Player text
        playerOne.setText(userCreated.getString("name"));
        playerTwo.setText(userInvited.getString("name"));

        //Accept, Decline, or Cancel request

        decline.setTextSize(25);
        accept.setTextSize(25);
        final Intent home = new Intent(this, UserInvites.class);
        if (request.equals("Pending")) {

            selectWinner.setText("Please accept or decline this match.");
            selectWinner.setVisibility(View.VISIBLE);
            decline.setVisibility(View.VISIBLE);
            accept.setVisibility(View.VISIBLE);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invite.put("request", true);
                    invite.saveInBackground();
                    JSONObject data = null;
                    try {
                        data = new JSONObject();
                        data.put("title", "Both Players Have");
                        data.put("alert", "Accepted The Match. Good Luck!");
                        data.put("customdata", "My String");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ParsePush.subscribeInBackground(channel);
                    ParsePush push = new ParsePush();
                    push.setChannel(channel);
                    push.setData(data);
                    push.sendInBackground();
                    startActivity(home);
                    finish();
                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invite.put("request", false);
                    invite.saveInBackground();
                    startActivity(home);
                    finish();
                }
            });
        } else if (request.equals("Accepted")) {

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WhoWon();
                    if (showRating == true) {
                        openRating();
                    }
                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(home);
                    finish();
                }
            });
        } else if (request.equals("Declined")) {
            selectWinner.setText("You declined this match.");
            selectWinner.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.GONE);
            decline.setVisibility(View.INVISIBLE);
            accept.setVisibility(View.INVISIBLE);

        } else if (request.equals("Sent")) {
            PopulateUserTopInvited(invite);

            if (showWon == true) {
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WhoWon();
                        if (showRating == true) {
                            openRating();
                        }
                    }
                });
            } else {
//                selectWinner.setText(request);
                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(home);
                    }
                });
                final Intent editInvite = new Intent(this, EditInvite.class);
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editInvite.putExtra("location", location);
                        editInvite.putExtra("time", time);
                        editInvite.putExtra("day", dayView.getText().toString());
                        editInvite.putExtra("year", yearOf.getText().toString());
                        editInvite.putExtra("sportplaying", sport);
                        editInvite.putExtra("objId", objId);
                        startActivity(editInvite);
                    }
                });
            }
        }

        //Send comment to parse server
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editComment.getText().toString() != "") {
                    List<HashMap<String, String>> listComments = new ArrayList<HashMap<String, String>>();
                    String userId = ParseUser.getCurrentUser().getObjectId();
                    String comment = editComment.getText().toString();
                    HashMap<String, String> fullComment = new HashMap<String, String>();

                    fullComment.put("comment", comment);
                    fullComment.put("user", userId);
                    System.out.println(fullComment.get("comment") + fullComment.get("user"));
                    if (invite.containsKey("comments")) {
                        invite.add("comments", fullComment);
                    } else {
                        listComments.add(fullComment);
                        invite.put("comments", listComments);
                    }
                    invite.saveInBackground();
                    JSONObject data = null;
                    try {
                        data = new JSONObject();
                        data.put("title", "You got a new message from " + MyResources.currentUserName);
                        data.put("alert", "\nFrom the game of " + sport +
                                "\n" + fullComment.get("comment"));
                        data.put("customdata", "My String");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(channel);
                    ParsePush push = new ParsePush();
                    //Set channel
                    if (userInvited == ParseUser.getCurrentUser()) {
                        push.setChannel("user_" + userCreated.getObjectId());
                    } else {
                        push.setChannel("user_" + userInvited.getObjectId());
                    }

                    push.setData(data);
                    push.sendInBackground();
                    Toast.makeText(v.getContext(), "Your comment has been sent", Toast.LENGTH_LONG).show();

                    //Create and display text

                    //Create Layout Parameters
                    LinearLayout.LayoutParams par = new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    //Create textviews
                    TextView text = new TextView(v.getContext());
                    text.setText(MyResources.currentUserName + ": " + comment);
                    text.setLayoutParams(par);

                    //add to layout
                    commentLinear.addView(text);
                    editComment.setText("");
                } else {
                    Toast.makeText(v.getContext(), "Please make sure the comment field is not blank", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Initialize google maps
        if (!location.equals("No Address Selected")) {
            map = "http://maps.google.co.in/maps?q=" + location;
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            locationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(i);
                }
            });
        }
    }

    private void WhoWon() {
        //Find rank on table
        System.out.println(winningPlayer);
        try {
            ParseQuery<ParseObject> rankings = ParseQuery.getQuery("ranks");
            int pointsAwarded = 0;
            if (playerOne.isChecked() == true) {
                if (winningPlayer.contains(userCreated.getObjectId().toString())) {
                    return;
                } else {
                    winningPlayer = userCreated.getObjectId().toString();
                    finished += ParseUser.getCurrentUser().getObjectId().toString();
                    try {
                        rank = rankings.get(createdObject);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    invite.put("finished", finished);
                    invite.put("whoWon", winningPlayer);
                    invite.saveInBackground();
                    pointsAwarded = invitedRank * 5;

                }
            } else if (playerTwo.isChecked() == true) {
                if (winningPlayer.contains(userInvited.getObjectId().toString())) {
                    return;
                } else {
                    winningPlayer = userInvited.getObjectId().toString();
                    finished += ParseUser.getCurrentUser().getObjectId().toString();
                    try {
                        rank = rankings.get(invitedObject);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    invite.put("finished", finished);
                    invite.put("whoWon", winningPlayer);
                    invite.saveInBackground();
                    pointsAwarded = invitedRank * 5;
                    System.out.println(pointsAwarded);
                    System.out.println(invitedRank);
                }
            } else {
                showRating = false;
                return;
            }
            int score = rank.getInt("Score");
            rank.put("Score", score + pointsAwarded);
            System.out.println(pointsAwarded);
            rank.saveInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void PopulateComments(HashMap<String, String> d) {
        String userId = d.get("user");
        String commentText = d.get("comment");
        ParseQuery<ParseUser> Iquery = ParseUser.getQuery();
        String name = "";
        try {
            ParseUser commentUser = Iquery.get(userId);
            name = commentUser.getString("name");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Create Layout Parameters
        LinearLayout.LayoutParams par = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //Create TextViews
        TextView text = new TextView(this);
        text.setText(name + ": " + commentText);
        text.setLayoutParams(par);

        //Add to view
        commentLinear.addView(text);
    }

    private void PopulateUserTopCreated(ParseObject invite) {
        nameUser.setText(userCreated.getString("name"));
        genderUser.setText(userCreated.getString("gender"));
        String sportText = "";
        List<String> sports = userCreated.getList("sports");
        for (String x : sports) {
            if (sports.indexOf(x) == sports.size()) {
                sportText += x;
            } else {
                sportText += x + ", ";
            }
        }
        sportsText.setText(sportText);
        playerTwo.setText(nameUser.getText().toString());

        locationUser.setText(userCreated.getString("city").substring(0, userCreated.getString("city").length() - 6));
        String photoUrl = userCreated.getString("profilePicture");
        try {
            setProfilePhoto(photoUrl, invitePhoto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PopulateUserTopInvited(ParseObject invite) {
        nameUser.setText(userInvited.getString("name"));
        genderUser.setText(userInvited.getString("gender"));
        String sportText = "";
        List<String> sports = userInvited.getList("sports");
        for (String x : sports) {
            if (sports.indexOf(x) == sports.size()) {
                sportText += x;
            } else {
                sportText += x + ", ";
            }
        }
        sportsText.setText(sportText);
        playerTwo.setText(nameUser.getText().toString());

        locationUser.setText(userInvited.getString("city").substring(0, userInvited.getString("city").length() - 6));
        String photoUrl = userInvited.getString("profilePicture");
        try {
            setProfilePhoto(photoUrl, invitePhoto);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setWinner(String winningPlayer) {
        if (winningPlayer.contains(userInvited.getObjectId().toString())) {
            selectWinner.setText(userInvited.getString("name") + " won this match.");
        } else {
            selectWinner.setText(userCreated.getString("name") + " won this match.");
        }
    }

    private void openRating() {
        final ParseUser user;
        if (ParseUser.getCurrentUser().getObjectId().equals(userInvited.getObjectId())) {
            user = userCreated;
        } else {
            user = userInvited;
        }
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setTitleColor(Color.TRANSPARENT);
        dialog.setTitle("Please Rate Player");
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout linear = new LinearLayout(this);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setBackgroundColor(Color.WHITE);
        ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setRating(5);
        ratingBar.animate();
        //Create both buttons for Submit and Later
        Button submit = new Button(this);
        submit.setText("Submit");
        //Comment about player
        final EditText comment = new EditText(this);
        final Intent home = new Intent(this, UserInvites.class);
        comment.setSingleLine();
        comment.setHint("...");
        comment.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        submit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject rating = new ParseObject("Ratings");
                int ratingNum = Math.round(ratingBar.getRating());
                rating.put("Rating", ratingNum);
                rating.put("Person", user);
                rating.put("Comment", comment.getText().toString());
                rating.put("Name", MyResources.currentUserName);
                rating.saveInBackground();
                dialog.dismiss();
                startActivity(home);
                finish();
            }
        });
        linear.addView(ratingBar);
        linear.addView(comment);
        linear.addView(submit);
        dialog.setContentView(linear);
        dialog.show();
    }
}
