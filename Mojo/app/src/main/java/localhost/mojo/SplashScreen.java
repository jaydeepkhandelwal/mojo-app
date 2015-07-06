package localhost.mojo;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class SplashScreen extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{


    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private static final String TAG = "Splash";
    private Location mLastLocation = null;
    private Geocoder mGeocoder = null;
    private Map map = null;
    private GoogleApiClient mGoogleApiClient = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mGoogleApiClient = GoogleApiClientSingleton.getGoogleApiClient();
        mGoogleApiClient.registerConnectionFailedListener(this);
        mGoogleApiClient.registerConnectionCallbacks(this);
//       GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
////                .enableAutoManage(this, 0 /* clientId */, this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(LocationServices.API)
//                .addOnConnectionFailedListener(this)
//                .addConnectionCallbacks(this)
//                .build();
//        GoogleApiClientSingleton.Init(mGoogleApiClient);

//        new Handler().postDelayed(new Runnable() {
//
//            /*
//             * Showing splash screen with a timer. This will be useful when you
//             * want to show case your app logo / company
//             */
//
//            @Override
//            public void run() {
//                // This method will be executed once the timer is over
//                // Start your app main activity
//                Intent i = new Intent(SplashScreen.this, MainActivity.class);
//                startActivity(i);
//
//                // close this activity
//                finish();
//            }
//        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    void showAlertDialog(int title) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(title);
       // newFragment.setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Holo_Dialog_NoActionBar);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick() {
        finish();
        Log.i("FragmentAlertDialog", "Positive click!");
    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
        showAlertDialog(R.string.connection_alert_title);

    }
    @Override
    public void onConnectionSuspended(int arg) {

    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if(mGoogleApiClient.isConnected()) {
            switchToMainActivity();
        }
        else {
            showAlertDialog(R.string.connection_alert_title);
        }
    }

    public  void switchToMainActivity(){
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);

        // close this activity
        finish();
    }

    public void setCurrentLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
               mGoogleApiClient);

        if (mLastLocation != null) {

            mGeocoder = new Geocoder(this, Locale.getDefault());
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            try {
                // Loading map
                map = new Map();
                map.initilizeMap();
                // create marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps ");

                // adding marker
                GoogleMap googleMap = map.getGoogleMap();
                googleMap.addMarker(marker);


            } catch (Exception e) {
                e.printStackTrace();
            }


            switchToMainActivity();
        }
    }


    //
//
//            List<Address> addresses = null;
//            Address address = null;
//            try {
//                addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "Canont get current Address!");
//            }
//            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
////            String state = addresses.get(0).getAdminArea();
////            String country = addresses.get(0).getCountryName();
////            StringBuilder fullAddr = new StringBuilder("");
//
//            String fullAddr = "";
//            if (!isHint && addresses != null) {
//                address = addresses.get(0);
////                String primaryAddr = address.getAddressLine(0);
////                String subLocality = address.getSubLocality();
////                String city = address.getLocality();
////                fullAddr = primaryAddr + ", " + subLocality + ", " + city;
////                mAutoCompleteTextViewFrom.setText(fullAddr);
////                Request etaRequest = null;
////                Request priceRequest = null;
////                etaRequest = new Request();
////                etaRequest.setUrl(etaURL);
////                etaRequest.addParams("start_latitude", fromPlaceLat);
////                etaRequest.addParams("start_longitude", fromPlaceLng);
////                new HttpAsyncTask().execute(etaRequest, priceRequest);
//
//                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                    if(address.getAddressLine(i) != null) {
//                        fullAddr += address.getAddressLine(i);
//                    }
//                    if(i !=  address.getMaxAddressLineIndex() - 1) {
//                        fullAddr += ", ";
//                    }
////                    fullAddr.append(address.getAddressLine(i)).append(",");
//                }
//                if(fullAddr == ""){
//                    fromPlaceLat = null;
//                    fromPlaceLng = null;
//                }
//
//
//                mAutoCompleteTextViewFrom.setText(fullAddr);
//
//
//            }
//            else if (addresses != null) {
//                mAutoCompleteTextViewFrom.setHint("Current Location");
//                loadETAAndFare();
//            }
//
//

}