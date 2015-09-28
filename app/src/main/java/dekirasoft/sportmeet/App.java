package dekirasoft.sportmeet;

import android.app.Application;
import android.util.Log;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by Kota on 1/3/2015.
 */
public class App extends Application {


    public App() {

    }
    

   @Override
    public void onCreate(){
        super.onCreate();
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, "DvPuAXcMVuCEKsZUexyqFN05mMvmfQF31Y0p0w4y", "pRxdKoRRTm6yxvh5VuRnRq2LJuLUEX5awZujSnVQ");
        Parse.initialize(this, "lp3M4A6jDHHHvPSlGjrfnUeGgsvGzPJPRQMHCI2e", "U3KtOZvBWe9eXjjkvthelbEssLblKjXMJ2VzTFqJ");

       ParsePush.subscribeInBackground("", new SaveCallback() {
           @Override
           public void done(ParseException e) {
               if (e == null) {
                   Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
               } else {
                   Log.e("com.parse.push", "failed to subscribe for push", e);
               }
           }
       });

       ParseInstallation.getCurrentInstallation().saveInBackground();

       //Image Loader set up
       DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
               .cacheOnDisk(true).cacheInMemory(true)
               .imageScaleType(ImageScaleType.EXACTLY)
               .displayer(new FadeInBitmapDisplayer(300)).build();

       ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
               getApplicationContext())
               .defaultDisplayImageOptions(defaultOptions)
               .memoryCache(new WeakMemoryCache())
               .diskCacheSize(100 * 1024 * 1024).build();

       ImageLoader.getInstance().init(config);
       // End Image Loader set up



   }
}
