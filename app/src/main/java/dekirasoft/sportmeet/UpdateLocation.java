package dekirasoft.sportmeet;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.location.LocationListener;
import com.parse.ParseGeoPoint;

import java.io.IOException;
import java.security.Provider;
import java.util.List;

/**
 * Created by Kota on 1/8/2015.
 */
public class UpdateLocation extends Activity implements LocationListener {

    static final String LogTag = "GetLcoation";
    TextView _addressText, _locationText;
    Location _currentLocation;
    LocationManager _locationManager;
    ParseGeoPoint geo;
    String city, state, _locationProvider;
    ProgressDialog progressDialog;
    @Override
    public void onLocationChanged(Location location) {
            System.out.println("Location Changed");
        _currentLocation = location;
        if(_currentLocation == null){
            _locationText.setText("Unable to determine your location.");

        }
        else
        {
            _locationText.setText(String.format("{0}, {1}", _currentLocation.getLatitude(), _currentLocation.getLongitude()));
            geo.setLongitude(_currentLocation.getLongitude());
            geo.setLatitude(_currentLocation.getLatitude());
            AddressButton_OnClick();
            progressDialog.dismiss();
        }
    }

    public void OnProviderDisabled(String provider){

    }
    public void OnProviderEnabled(String provider){

    }
//    public void OnStatusChanged(String provider, GooglePlayServicesAvailabilityException status, Bundle extras){
//        System.out.println(LogTag + "{0}, {1}",provider, status);
//    }


    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.update_location);

        _locationText = (TextView)findViewById(R.id.usersCurrentLocation);
        _addressText = (TextView)findViewById(R.id.addressText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting your current location. ");
        progressDialog.isIndeterminate();
        progressDialog.show();

        InitializeLocationManager ();
    }


    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("First Attempt");
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if(!_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(this);
            TextView text = new TextView(this);
            text.setText("Please turn on Wireless Access under your location settings" +
                    " to retrieve your current location. If this is not available or is not working turn on your gps");
            Button button1 = new Button(this);
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.VERTICAL);
            l.addView(text);
            button1.setText("Okay");
            l.addView(button1);
            dialog.setContentView(l);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                }
            });
            dialog.show();
        }

        try {
            _locationManager.requestLocationUpdates(_locationProvider,0,0, (android.location.LocationListener) this);
            System.out.println("Done");
        } catch (Exception e){
            //failed first attempt
           e.printStackTrace();
            try{
                _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, (android.location.LocationListener) this);
            }catch (Exception ex){
                //failed second attempt
                ex.printStackTrace();
                try{
                    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, (android.location.LocationListener) this);
                } catch (Exception exc) {

                    exc.printStackTrace();
                    try {
                        _locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER ,0,0, (android.location.LocationListener) this);
                    } catch (Exception exec){
                        //failed fourth attempt. there is no hope
                        Toast.makeText(this, "An error ocurred. Please ensure that your gps is on. ", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        _locationManager.removeUpdates((android.location.LocationListener) this);
    }

    private void AddressButton_OnClick(){
        if(_currentLocation == null){
            Toast.makeText(this, "Please turn on gps to continue", Toast.LENGTH_LONG).show();
            finish();
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocation(_currentLocation.getLatitude(),_currentLocation.getLongitude(), 30);
            Address address = addressList.get(0);
            if(address != null){
                StringBuilder deviceAddress = new StringBuilder();
                for (int i = 1; i <address.getMaxAddressLineIndex(); i++){
                    deviceAddress.append(address.getAddressLine(i))
                            .append(",");
                    if(i == 1){
                        city = address.getAddressLine(1);
                        MyResources.editUserCity = city;
                        MyResources.changedAddres = true;
                        MyResources.geoAddress = geo;
                        NewUserSignUp.city = city;
                        NewUserSignUp.geo = geo;
                        NewUserSignUp._addressText.setText(city);
                        finish();
                    } if (i == 2){
                        state = address.getAddressLine(2);
                    }
                }
                _addressText.setText(deviceAddress.toString());
            }
            else {
                _addressText.setText("Unable to determine address");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void InitializeLocationManager(){
        try {
            _locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            Criteria criteriaForLocationService = new Criteria(){
                @Override
                public void setAccuracy(int accuracy) {
                    super.setAccuracy(accuracy);
                    accuracy = ACCURACY_COARSE;
                }
            };
            List<String> acceptableLocationProviders = _locationManager.getProviders(criteriaForLocationService, true);
            for(String i : acceptableLocationProviders){
                System.out.println(i);
            }
            if(acceptableLocationProviders.size() > 0){
                _locationProvider = acceptableLocationProviders.get(0);
            }
            else
            {
                _locationProvider = "";
            }
            System.out.println("Using " + _locationProvider + ",");
        } catch (Exception e){
            e.printStackTrace();
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(this);
            dialog.setTitle("Turn GPS on");
            Button button1 = new Button(this);
            button1.setText("Okay");
            dialog.setContentView(button1);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    finish();

                }

            });

            dialog.show();
        }
    }
}
