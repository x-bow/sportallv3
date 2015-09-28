package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class GooglePlayServicesActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "GooglePlayServicesActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;
    private Person currentPerson;
    SignInButton signInButton;
    TextView free_account, sign_in;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        getAllSports();
        signInButton = (SignInButton) findViewById(R.id.google_sign_in);
        free_account = (TextView) findViewById(R.id.free_account);
        sign_in = (TextView) findViewById(R.id.sign_in);
        sign_in.setOnClickListener(this);
        free_account.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                            // Optionally, add additional APIs and scopes if required.
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
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
//        mIsInResolution = false;
//        if (!mGoogleApiClient.isConnecting()) {
//            mGoogleApiClient.connect();
//        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO: Start making API requests.
        createUser();
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
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
        } catch (SendIntentException e) {
            retryConnecting();
        }
    }

    // Create and Sign in Parse User
    private void createUser() {
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String photoUrl = currentPerson.getImage().getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String name = currentPerson.getDisplayName();
            parseLogIn(email, name, photoUrl);
        }
    }

    private void parseLogIn(final String email, final String name, final String photoUrl) {
        final ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setPassword("zztop");
        user.setEmail(email);
        final Intent intentm = new Intent(this, NewUserSignUp.class);
        final Intent intent = new Intent(this, MainActivity.class);
//        user.signUpInBackground(new SignUpCallback() {
//
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    //Successfully signed up
//                    intentm.putExtra("name", name);
//                    intentm.putExtra("profilePicture", photoUrl);
////                    user.put("name", name);
////                    user.put("profilePicture", photoUrl);
//                    startActivity(intentm);
//                    finish();
//                } else {
        // try loggin in, sign up failed

        ParseUser.logInInBackground(email, "zztop", new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null) {
                    //Signed in Successfully
                    Toast.makeText(GooglePlayServicesActivity.this, "You have been signed in.", Toast.LENGTH_LONG).show();

                    startActivity(intent);
                    finish();
                } else {
                    try {
                        intentm.putExtra("name", name);
                        intentm.putExtra("photoUrl", photoUrl);
                        intentm.putExtra("email", email);
                        intentm.putExtra("userNew", true);
                        startActivity(intentm);
                        finish();
                    } catch (Exception y) {
                        Toast.makeText(GooglePlayServicesActivity.this, "There was an error signing in.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in:
//                mGoogleApiClient.connect();
                break;
            case R.id.free_account:
                startActivity(new Intent(this, SignUp.class));
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                break;
            case R.id.sign_in:
                startActivity(new Intent(this, Login.class));
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                break;
        }
    }

    public void getAllSports() {
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
                } else {
                    e.printStackTrace();
                }
            }
        });

    }
}
