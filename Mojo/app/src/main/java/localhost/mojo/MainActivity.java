package localhost.mojo;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity
{

    private final static String etaURL = "https://fierce-refuge-4051.herokuapp.com/eta";
    private final static String priceURL = "https://fierce-refuge-4051.herokuapp.com/price";
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdaperFrom;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdaperTo;

    private AutoCompleteTextView mAutoCompleteTextViewFrom;
    private AutoCompleteTextView mAutoCompleteTextViewTo;
   //private ImageView mRefreshButton;
    private APIResponseAdapter mAPIResponseAdapter;
    private String TAG = "MainActivity";
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(8.0689, 77.5523), new LatLng(33.24902, 76.82704));
    private double bangaloreLat = 12.9667;
    private double bangaloreLng = 77.5667;
    private List<APIResponseData> mAPIResponseDataList = new ArrayList<APIResponseData>();
    private ListView mAPIResponseListView;
    private String fromPlaceLat = null;
    private String fromPlaceLng = null;
    private String toPlaceLat = null;
    private String toPlaceLng = null;
    private String currentLocationLat = null;
    private String currentLocationLng = null;
    private Location mLastLocation = null;
    private Geocoder mGeocoder = null;

    private String OLA = "Ola";
    private String UBER = "Uber";
    private String TFS = "Tfs";
    private GoogleMap googleMap = null;
    private boolean isMapVisible = true;
    private FrameLayout mapFragment = null;
    private enum RESPONSE_TYPE {
        ETA,
        PRICE
    }
    AutoCompleteViewTextWatcher mAutoCompleteViewFromTextWatcher = null;
    AutoCompleteViewTextWatcher mAutoCompleteViewToTextWatcher = null;

    SwipeRefreshLayout mSwipeRefreshLayout;
    LoadProgressDialog mLoadProgressDialog = null;
    LoadProgressDialog mRefreshProgressDialog = null;
    LoadProgressDialog mCurrentProgressDialog = null;
    ProgressBar mProgressBar = null;
    TranslateAnimation mAnimation = null;
    TextView mProgressBarTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = GoogleApiClientSingleton.getGoogleApiClient();


        Drawable imgClearButton  = ContextCompat.getDrawable(this, R.drawable.abc_ic_clear_mtrl_alpha);

        ColorFilter filter = new LightingColorFilter( Color.rgb(0, 0, 0), Color.rgb(255, 255, 255));
        imgClearButton.setColorFilter(filter);

//        SpannableString ss = new SpannableString("test");
//        Drawable d = ContextCompat.getDrawable(this, R.drawable.abc_ic_clear_mtrl_alpha);
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//        ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        mAutoCompleteTextViewFrom.setText(ss);
//
        // Retrieve the AutoCompleteTextView that will display Place suggestions
        mAutoCompleteTextViewFrom = (AutoCompleteTextView)
                findViewById(R.id.from_location);
        mAutoCompleteTextViewFrom.clearFocus();
        mPlaceAutoCompleteAdaperFrom = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_INDIA, 1, null);
        mAutoCompleteTextViewFrom.setAdapter(mPlaceAutoCompleteAdaperFrom);
        mAutoCompleteTextViewFrom.setOnItemClickListener(mAutocompleteClickListener);
        mAutoCompleteTextViewFrom.setCompoundDrawablesWithIntrinsicBounds(null, null,
                imgClearButton, null);

        mAutoCompleteTextViewFrom.setOnTouchListener(mAutoCompleteTextViewTouchListener);
        mAutoCompleteTextViewFrom.setOnKeyListener(mAutoCompleteTextViewKeyListener);
       // mAutoCompleteTextViewFrom.setImeActionLabel("Get Cabs", KeyEvent.KEYCODE_ENTER);

        mapFragment = (FrameLayout) findViewById(R.id.map);
        try {
            // Loading map

            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        setCurrentLocation(false);


        mAutoCompleteTextViewTo = (AutoCompleteTextView)
                findViewById(R.id.to_location);
        mAutoCompleteTextViewTo.requestFocus();
        mPlaceAutoCompleteAdaperTo = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_INDIA, 2, null);

        mAutoCompleteTextViewTo.setAdapter(mPlaceAutoCompleteAdaperTo);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutoCompleteTextViewTo.setOnItemClickListener(mAutocompleteClickListener);

        mAutoCompleteTextViewTo.setCompoundDrawablesWithIntrinsicBounds(null, null,
                imgClearButton, null);
        mAutoCompleteTextViewTo.setOnTouchListener(mAutoCompleteTextViewTouchListener);
        mAutoCompleteTextViewTo.setOnKeyListener(mAutoCompleteTextViewKeyListener);
      //  mAutoCompleteTextViewTo.setImeActionLabel("Get Cabs", KeyEvent.KEYCODE_ENTER);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mSwipeRefreshLayoutListener);
        mAPIResponseListView = (ListView) findViewById(R.id.api_response);
        mAPIResponseAdapter = new APIResponseAdapter(this, R.layout.api_response_item_new, mAPIResponseDataList);
        mAPIResponseListView.setAdapter(mAPIResponseAdapter);
        mAPIResponseListView.setOnItemClickListener(mAPIResponseListViewClickListener);

        mAutoCompleteViewFromTextWatcher = new AutoCompleteViewTextWatcher();
        mAutoCompleteViewToTextWatcher = new AutoCompleteViewTextWatcher();

        mAutoCompleteTextViewFrom.addTextChangedListener(mAutoCompleteViewFromTextWatcher);
        mAutoCompleteTextViewTo.addTextChangedListener(mAutoCompleteViewToTextWatcher);



//        mRefreshButton = (ImageView) findViewById(R.id.refresh_button);
//        mRefreshButton.setOnClickListener(mRefreshButtonClickListener);
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        int height = displaymetrics.heightPixels;
//        int width = displaymetrics.widthPixels;
//
//        mRefreshButton.getLayoutParams().height = height/10;
//        mRefreshButton.getLayoutParams().width = height/10;

         CabNamesMapping.init();
        mLoadProgressDialog = new LoadProgressDialog();
        mLoadProgressDialog.createProgressDialog(this, "Please Wait...", "Looking for cabs...");
        mRefreshProgressDialog = new LoadProgressDialog();
        mRefreshProgressDialog.createProgressDialog(this, "Please Wait...", "Refreshing details...");
        googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mProgressBar = (ProgressBar)  findViewById(R.id.progress_bar);
        mProgressBarTextView = (TextView) findViewById(R.id.progress_bar_text);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgressBarTextView.setVisibility(View.GONE);

//        mAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
//                Animation.RELATIVE_TO_PARENT,0.7f,Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0
//                );
//        mAnimation.setDuration(700);
//
//        mAnimation.setRepeatCount(Animation.INFINITE);
//        mAnimation.setRepeatMode(2);
//        mAnimation.setFillAfter(true);

    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {

        @Override
        public boolean onMyLocationButtonClick()
        {
            mAutoCompleteTextViewFrom.setText("");
            currentLocationLng = null;
            currentLocationLat = null;
            setCurrentLocation(false);
           loadETAAndFare(false);
            //TODO: Any custom actions
            return false;
        }



    };

    public Bitmap scaleMarker(int respource, int scaleFactor) {
        LevelListDrawable d=(LevelListDrawable) getResources().getDrawable(respource);
        d.setLevel(1234);
        BitmapDrawable bd=(BitmapDrawable) d.getCurrent();
        Bitmap b=bd.getBitmap();
        Bitmap scaledBitmap =Bitmap.createScaledBitmap(b, b.getWidth() / scaleFactor, b.getHeight() / scaleFactor, false);
        return scaledBitmap;
    }
    private Bitmap scaleImage(Resources res, int id, int lessSideSize) {
        Bitmap b = null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, id, o);

        float sc = 0.0f;
        int scale = 1;
        // if image height is greater than width
        if (o.outHeight > o.outWidth) {
            sc = o.outHeight / lessSideSize;
            scale = Math.round(sc);
        }
        // if image width is greater than height
        else {
            sc = o.outWidth / lessSideSize;
            scale = Math.round(sc);
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        b = BitmapFactory.decodeResource(res, id, o2);
        return b;
    }


    public void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
//                Toast.makeText(getApplicationContext(),
//                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
//                        .show();
            }
        }
    }
    class AutoCompleteViewTextWatcher implements TextWatcher {


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (this == mAutoCompleteViewFromTextWatcher) {
                   if(start != 0) {
                       fromPlaceLat = null;
                       fromPlaceLng = null;
                   }
                    if(s.toString().equals("")) {
                        mAutoCompleteTextViewFrom.setHint("Where are you?");
                      //  setCurrentLocation(true);
                    }



                } else if(this == mAutoCompleteViewToTextWatcher){
                    toPlaceLat = null;
                    toPlaceLng = null;
                }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void loadETAAndFare(boolean isSwiped){
        if(fromPlaceLat != null && fromPlaceLng != null && toPlaceLng != null && toPlaceLat != null) {
            mAPIResponseDataList.clear();
            mAPIResponseAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
           // mProgressBar.startAnimation(mAnimation);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBarTextView.setVisibility(View.VISIBLE);

            //  mSwipeRefreshLayout.setRefreshing(false);
            if(!isSwiped) {

                mCurrentProgressDialog = mLoadProgressDialog;
               //     mLoadProgressDialog.showProgressDialog();
//                mLoadProgressDialog = new LoadProgressDialog();
//                mLoadProgressDialog.showProgressDialog(this, "Please Wait...", "Looking for cabs...");
            }
            else {
                mCurrentProgressDialog = mRefreshProgressDialog;
       //         mRefreshProgressDialog.showProgressDialog();
//                mLoadProgressDialog = new LoadProgressDialog();
//                mLoadProgressDialog.showProgressDialog(this, "Please Wait...", "Refreshing details...");
            }

            Request etaRequest = null;
            Request priceRequest = null;
            etaRequest = new Request();
            etaRequest.setUrl(etaURL);
            etaRequest.addParams("start_latitude", fromPlaceLat);
            etaRequest.addParams("start_longitude", fromPlaceLng);

            priceRequest = new Request();
            priceRequest.setUrl(priceURL);
            priceRequest.addParams("start_latitude", fromPlaceLat);
            priceRequest.addParams("start_longitude", fromPlaceLng);
            priceRequest.addParams("end_latitude", toPlaceLat);
            priceRequest.addParams("end_longitude", toPlaceLng);
            new HttpAsyncTask().execute(etaRequest, priceRequest);
        }
    }


    private ImageView.OnClickListener mRefreshButtonClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadETAAndFare(false);
        }
    };


    private SwipeRefreshLayout.OnRefreshListener mSwipeRefreshLayoutListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSwipeRefreshLayout.setRefreshing(false);
          loadETAAndFare(true);
        }
    };

    private AutoCompleteTextView.OnKeyListener mAutoCompleteTextViewKeyListener = new AutoCompleteTextView.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                loadETAAndFare(false);
                return true;
            }
            return false;
        }
    };

    private AutoCompleteTextView.OnTouchListener mAutoCompleteTextViewTouchListener = new AutoCompleteTextView.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)v;

            Drawable imgClearButton = autoCompleteTextView.getCompoundDrawables()[2];
            if (autoCompleteTextView.getCompoundDrawables()[2] == null) {
                return false;
            }
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }
            if (event.getX() > autoCompleteTextView.getWidth() - autoCompleteTextView.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
                autoCompleteTextView.setText("");
                if(autoCompleteTextView.getId() == mAutoCompleteTextViewFrom.getId()) {
                    fromPlaceLng = null;
                    fromPlaceLat = null;
                    mAutoCompleteTextViewFrom.setHint("Where are you?");
                    //  setCurrentLocation(true);
                }
                else if(autoCompleteTextView.getId() == mAutoCompleteTextViewTo.getId()) {
                    toPlaceLng = null;
                    toPlaceLat = null;

                }

                // autoCompleteTextView.setCompoundDrawables(null, null, null, null);
            }
            return false;
        }
    };


    void showAlertDialog(int title) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(title);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPositiveClick() {
        // Do stuff here.
        Log.i("FragmentAlertDialog", "Positive click!");
    }


    public void setCamera(GoogleMap googleMap, double latitude, double longitude){
        if(googleMap != null) {
            googleMap.clear();
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)).zoom(14).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
    public void setCurrentLocation(boolean isHint){

        if(currentLocationLat != null && currentLocationLng != null) {

                fromPlaceLng = currentLocationLng;
                fromPlaceLat = currentLocationLat;
                if(isMapVisible && googleMap != null ) {
                    googleMap.setMyLocationEnabled(true); // false to disable
                }
        //        loadETAAndFare();
                return;
        }

        mAutoCompleteTextViewFrom.setText("");
        mAutoCompleteTextViewFrom.setHint("Getting current Location...");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            mGeocoder = new Geocoder(this, Locale.getDefault());
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            fromPlaceLat = Double.toString(latitude);
            fromPlaceLng = Double.toString(longitude);
            currentLocationLat = fromPlaceLat;
            currentLocationLng = fromPlaceLng;

            if(isHint) {
                mAutoCompleteTextViewFrom.setHint("Current Location");
                if(isMapVisible && googleMap != null ) {
                    googleMap.setMyLocationEnabled(true); // false to disable
                    setCamera(googleMap, latitude, longitude);

                }
               // loadETAAndFare();
            }
            else {

                String fullAddr = "";
                List<Address> addresses = null;
                Address address = null;
                try {
                    addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Canont get current Address!");
                }
                if (addresses != null) {
                    address = addresses.get(0);
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        if (address.getAddressLine(i) != null) {
                            fullAddr += address.getAddressLine(i);
                        }
                        if (i != address.getMaxAddressLineIndex() - 1) {
                            fullAddr += ", ";
                        }
                    }
                    if (fullAddr == "") {
                        currentLocationLat = null;
                        currentLocationLng = null;
                        fromPlaceLat = null;
                        fromPlaceLng = null;
                        showAlertDialog(R.string.location_alert_title);
                        mAutoCompleteTextViewFrom.setHint("Where are you?");

                    }

                    mAutoCompleteTextViewFrom.setAdapter(null);
                    mAutoCompleteTextViewFrom.setText(fullAddr);
                    mAutoCompleteTextViewFrom.setAdapter(mPlaceAutoCompleteAdaperFrom);
                    if(isMapVisible && googleMap != null) {
                        googleMap.setMyLocationEnabled(true); // false to disable
                        setCamera(googleMap, latitude, longitude);

                    }
                    //loadETAAndFare(false);
                }
                else {
                    currentLocationLat = null;
                    currentLocationLng = null;
                    fromPlaceLat = null;
                    fromPlaceLng = null;
                    showAlertDialog(R.string.location_alert_title);
                    mAutoCompleteTextViewFrom.setHint("Where are you?");
                }
            }

        }
        else {

            showAlertDialog(R.string.location_alert_title);
            mAutoCompleteTextViewFrom.setHint("Where are you?");
            if(isMapVisible &&  googleMap != null ) {
            // create marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(bangaloreLat, bangaloreLng)).title(" ");
               // marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker_icon)));
                // adding marker
                googleMap.addMarker(marker);

            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
       // mGoogleApiClient.connect();
    }

    @Override
    public  void onResume(){
        super.onResume();
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }
    @Override
    public void onStop() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();

    }

    public void startNewActivity(Context context, String packageName, String deepLink) {

        PackageManager pm = MainActivity.this.getPackageManager();
        PackageInfo packageInfo = null;
        Intent intent = null;
        try {
            if(packageName != null) {
                packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            }
            else if (deepLink != null) {

                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(deepLink));

            }

            if (intent != null) {
            /* We found the activity now start the activity */
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);



            } else if(packageName != null) {

                intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(intent);

            }

            // Do something awesome - the app is installed! Launch App.
       } catch (PackageManager.NameNotFoundException e) {
             /* Bring user to the market or let them choose an app? */
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(intent);


        }
    }



    private AdapterView.OnItemClickListener mAPIResponseListViewClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            APIResponseAdapter apiResponseAdapter = (APIResponseAdapter) parent.getAdapter();

            final APIResponseData item = apiResponseAdapter.getItem(position);
            if (item.getCarProvider() != null && item.getCarProvider().equalsIgnoreCase("uber")) {
                String deeplink = "uber://";
                if(fromPlaceLat != null && fromPlaceLng != null) {
                    deeplink += "?action=setPickup&pickup[latitude]=" + fromPlaceLat + "&pickup[longitude]=" + fromPlaceLng;


                    if (toPlaceLng != null && toPlaceLat != null) {
                        deeplink += "&dropoff[latitude]="+toPlaceLat+"&dropoff[longitude]="+ toPlaceLng;
                    }
                }
                startNewActivity(MainActivity.this, "com.ubercab", deeplink);
            }
            else if (item.getCarProvider() != null && item.getCarProvider().equalsIgnoreCase("ola")) {
                startNewActivity(MainActivity.this, "com.olacabs.customer", null);
            }
            else if (item.getCarProvider() != null && item.getCarProvider().equalsIgnoreCase("tfs")) {
                startNewActivity(MainActivity.this, "com.tfs.consumer", null);
            }

        }
    };

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            PlaceAutoCompleteAdapter  placeAutoCompleteAdapter = (PlaceAutoCompleteAdapter)parent.getAdapter();

            final PlaceAutoCompleteAdapter.PlaceAutocomplete item = placeAutoCompleteAdapter.getItem(position);
            final Integer locationType = placeAutoCompleteAdapter.getLocationType();
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);


            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */


            if(locationType == 1) {
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mFromLocationCallback);
//            Toast.makeText(getApplicationContext(), "Clicked: " + item.description,
//                    Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
                mAutoCompleteTextViewFrom.setSelection(0);
            }
           else {
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mToLocationCallback);
                mAutoCompleteTextViewTo.setSelection(0);
//            Toast.makeText(getApplicationContext(), "Clicked: " + item.description,
//                    Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
            }
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mFromLocationCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            double latitude = place.getLatLng().latitude;
            double longitude = place.getLatLng().longitude;
           fromPlaceLat = Double.toString(latitude);
           fromPlaceLng =  Double.toString(longitude);
            setCamera(googleMap,latitude,longitude);
            loadETAAndFare(false);
//            Request etaRequest = null;
//            Request priceRequest = null;
//
//            if(toPlaceLat != null && toPlaceLng != null) {
//                etaRequest = new Request();
//                etaRequest.setUrl(etaURL);
//                etaRequest.addParams("start_latitude", fromPlaceLat);
//                etaRequest.addParams("start_longitude", fromPlaceLng);
//
//                priceRequest = new Request();
//                priceRequest.setUrl(priceURL);
//                priceRequest.addParams("start_latitude", fromPlaceLat);
//                priceRequest.addParams("start_longitude", fromPlaceLng);
//                priceRequest.addParams("end_latitude", toPlaceLat);
//                priceRequest.addParams("end_longitude", toPlaceLng);
//
//                new HttpAsyncTask().execute(etaRequest,priceRequest);
//
//            }






            Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }


    };
    private ResultCallback<PlaceBuffer> mToLocationCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();

                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            toPlaceLat = Double.toString(place.getLatLng().latitude);
            toPlaceLng =  Double.toString(place.getLatLng().longitude);
            loadETAAndFare(false);
//
//            if(fromPlaceLat != null && fromPlaceLng != null) {
//                Request etaRequest = null;
//                Request priceRequest = null;
//                etaRequest = new Request();
//                etaRequest.setUrl(etaURL);
//                etaRequest.addParams("start_latitude", fromPlaceLat);
//                etaRequest.addParams("start_longitude", fromPlaceLng);
//
//                priceRequest = new Request();
//                priceRequest.setUrl(priceURL);
//                priceRequest.addParams("start_latitude", fromPlaceLat);
//                priceRequest.addParams("start_longitude", fromPlaceLng);
//                priceRequest.addParams("end_latitude", toPlaceLat);
//                priceRequest.addParams("end_longitude", toPlaceLng);
//
//
//                new HttpAsyncTask().execute(etaRequest,priceRequest);
//            }

            Log.i(TAG, "Place details received: " + place.getName());

            places.release();
        }


    };




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class HttpAsyncTask extends AsyncTask<Request, Void, HashMap<RESPONSE_TYPE,String>> {
        private volatile boolean running = true;

        @Override
        protected HashMap<RESPONSE_TYPE,String> doInBackground(Request... requests) {

            while (running) {
                ServiceHandler serviceHandler = new ServiceHandler();
                Request etaRequest = requests[0];
                Request priceRequest = requests[1];
                HashMap<RESPONSE_TYPE, String> responseMap = new HashMap<RESPONSE_TYPE, String>();
                responseMap.put(RESPONSE_TYPE.ETA, null);
                responseMap.put(RESPONSE_TYPE.PRICE, null);
                if (etaRequest != null) {
                    String result = serviceHandler.makeServiceCall(etaRequest.getUrl(), 1, etaRequest.getRequestParams());
                    responseMap.put(RESPONSE_TYPE.ETA, result);

                }
                if (priceRequest != null) {
                    String result = serviceHandler.makeServiceCall(priceRequest.getUrl(), 1, priceRequest.getRequestParams());
                    responseMap.put(RESPONSE_TYPE.PRICE, result);
                }

                return responseMap;
            }
            return null;


        }
        public HttpAsyncTask() {
            ProgressDialog dialog = mCurrentProgressDialog.getDialog();
            if(dialog != null) {
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // actually could set running = false; right here, but I'll
                        // stick to contract.
                        cancel(true);
                    }
                });
            }

        }


        @Override
        protected void onPreExecute() {

        //    mCurrentProgressDialog.showProgressDialog();

        }

        @Override
        protected void onCancelled() {
            running = false;
        }


        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(HashMap<RESPONSE_TYPE,String> responseMap) {

           // mProgressBar.clearAnimation();
            mProgressBar.setVisibility(View.GONE);
            mProgressBarTextView.setVisibility(View.GONE);


            //  mSwipeRefreshLayout.setRefreshing(false);
//            if (mCurrentProgressDialog != null) {
//                mCurrentProgressDialog.closeProgressDialog();
//            }
            InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            String etaResponseStr = responseMap.get(RESPONSE_TYPE.ETA);
            String priceResponseStr = responseMap.get(RESPONSE_TYPE.PRICE);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            EtaResponse etaResponse = null;
            PriceResponse priceResponse = null;

            try {
                System.out.println(etaResponseStr);
                if (etaResponseStr != null) {
                    etaResponse = mapper.readValue(etaResponseStr, EtaResponse.class);
                }
                if (priceResponseStr != null) {
                    priceResponse = mapper.readValue(priceResponseStr, PriceResponse.class);
                }


            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            MainActivity.this.mAPIResponseDataList.clear();
            APIResponseData headerData = null;
            List<EtaData> etaDataList = null;
            List<PriceData> priceDataList = null;
            List<APIResponseData> APIResponseDataList;
            APIResponseData apiResponseData;
            String carCategory = null;
            int totalCabs = 0;
            if (etaResponse.getOla() != null && etaResponse.getOla().getDataList() != null && etaResponse.getOla().getDataList().size() != 0) {

//                headerData = new APIResponseData();
//                headerData.setCarProvider(OLA);
//                headerData.setEtaData(null);
//                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getOla().getDataList();

                APIResponseDataList = new ArrayList<APIResponseData>();
                if (priceResponse != null && priceResponse.getOla() != null && priceResponse.getOla().getDataList() != null) {
                    priceDataList = priceResponse.getOla().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    totalCabs++;
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(OLA);
                    if (priceDataList != null) {
                        for (PriceData tempPriceData : priceDataList) {
                            if (tempPriceData.getCarCategory().equalsIgnoreCase(temp.getCarCategory())) {
                                apiResponseData.setPriceData(tempPriceData);
                                break;
                            }
                        }
                    }
                    APIResponseDataList.add(apiResponseData);


                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }
            if (etaResponse.getUber() != null && etaResponse.getUber().getDataList() != null && etaResponse.getUber().getDataList().size() != 0) {

//                headerData = new APIResponseData();
//                headerData.setCarProvider("Uber");
//                headerData.setEtaData(null);
//                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getUber().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                if (priceResponse != null) {
                    priceDataList = priceResponse.getUber().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    totalCabs++;
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(UBER);
                    if (priceDataList != null) {
                        for (PriceData tempPriceData : priceDataList) {
                            if (tempPriceData.getCarCategory().equalsIgnoreCase(temp.getCarCategory())) {
                                apiResponseData.setPriceData(tempPriceData);
                                break;
                            }
                        }
                    }
                    APIResponseDataList.add(apiResponseData);


                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }

            if (etaResponse.getTfs() != null && etaResponse.getTfs().getDataList() != null && etaResponse.getTfs().getDataList().size() != 0) {

//                headerData = new APIResponseData();
//                headerData.setCarProvider("Taxi For Sure");
//                headerData.setEtaData(null);
//                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getTfs().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                if (priceResponse != null) {
                    priceDataList = priceResponse.getTfs().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    totalCabs++;
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(TFS);
                    if (priceDataList != null) {
                        for (PriceData tempPriceData : priceDataList) {
                            if (tempPriceData.getCarCategory().equalsIgnoreCase(temp.getCarCategory())) {
                                apiResponseData.setPriceData(tempPriceData);
                                break;
                            }
                        }
                    }
                    APIResponseDataList.add(apiResponseData);


                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }

            mAPIResponseAdapter.notifyDataSetChanged();
            //mRefreshButton.setVisibility(View.VISIBLE);
            //  mapFragment.setVisibility(View.GONE);


            if (totalCabs > 0) {
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            } else {
                mSwipeRefreshLayout.setVisibility(View.GONE);
                showAlertDialog(R.string.no_cab_alert);

            }

            if (googleMap != null) {

                double latitude = Double.parseDouble(fromPlaceLat);
                double lonitude = Double.parseDouble(fromPlaceLng);
                int mins;
                for (int i = 1; i <= totalCabs; i++) {
                    double offset = i / 1000d;
                    if(i % 4  == 0) {
                        latitude = latitude + offset;
                        lonitude = lonitude + offset;
                    }
                    else if (i % 4 == 1) {
                        latitude = latitude - offset;
                        lonitude = lonitude + offset;
                    }
                    else if (i % 4 == 2){
                        latitude = latitude + offset;
                        lonitude = lonitude - offset;
                    }
                    else {
                        latitude = latitude - offset;
                        lonitude = lonitude -   offset;
                    }

                    MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, lonitude)).title(" ");
//                    Bitmap icon = scaleMarker(R.drawable.car,5);
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_brown));
                    googleMap.addMarker(marker);


                }


        }

        }



    }
}
