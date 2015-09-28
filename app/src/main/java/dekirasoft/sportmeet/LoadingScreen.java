package dekirasoft.sportmeet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Kota on 1/21/2015.
 */
public class LoadingScreen extends Activity {

    Intent errorReturn = new Intent(this, GooglePlayServicesActivity.class);
    boolean sleep = true;
    int sleepCount = 0;


    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.loading_screen);

        ParseUser user = ParseUser.getCurrentUser();
        do {
            if (user.containsKey("city")) {
                System.out.println("You made it");
                sleep = false;
            } else {
                    SystemClock.sleep(3000);
                    sleepCount ++;
                    if(sleepCount == 3){
                        Toast.makeText(this, "There is an issue with processing your request." +
                                "Please send a message to Sportall if the issue continues ", Toast.LENGTH_LONG);
                        startActivity(errorReturn);
                        finish();
                    }

            }
        } while(sleep == true);

        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
        finish();



    }


}
