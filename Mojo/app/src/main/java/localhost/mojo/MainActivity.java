package localhost.mojo;

import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static String etaURL = "https://fierce-refuge-4051.herokuapp.com/eta";
    private final static String priceURL = "https://fierce-refuge-4051.herokuapp.com/price";
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdaperFrom;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdaperTo;

    private AutoCompleteTextView mAutoCompleteTextViewFrom;
    private AutoCompleteTextView mAutoCompleteTextViewTo;
    private ImageView mRefreshButton;
    private APIResponseAdapter mAPIResponseAdapter;
    private String TAG = "MainActivity";
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(8.0689, 77.5523), new LatLng(33.24902, 76.82704));

    private List<APIResponseData> mAPIResponseDataList = new ArrayList<APIResponseData>();
    private ListView mAPIResponseListView;
    private String fromPlaceLat = null;
    private String fromPlaceLng = null;
    private String toPlaceLat = null;
    private String toPlaceLng = null;
    private Location mLastLocation = null;
    private Geocoder mGeocoder = null;

    private String OLA = "Ola";
    private String UBER = "Uber";
    private String TFS = "Tfs";

    private enum RESPONSE_TYPE {
        ETA,
        PRICE
    }
    AutoCompleteViewTextWatcher mAutoCompleteViewFromTextWatcher = null;
    AutoCompleteViewTextWatcher mAutoCompleteViewToTextWatcher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        Drawable imgClearButton  = ContextCompat.getDrawable(this, R.drawable.abc_ic_clear_mtrl_alpha);
        ColorFilter filter = new LightingColorFilter( Color.rgb(0,0,0), Color.rgb(255,165,0));
        imgClearButton.setColorFilter(filter);

        // Retrieve the AutoCompleteTextView that will display Place suggestions
        mAutoCompleteTextViewFrom = (AutoCompleteTextView)
                findViewById(R.id.from_location);

        mPlaceAutoCompleteAdaperFrom = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_INDIA, 1, null);
        mAutoCompleteTextViewFrom.setAdapter(mPlaceAutoCompleteAdaperFrom);
        mAutoCompleteTextViewFrom.setOnItemClickListener(mAutocompleteClickListener);

        mAutoCompleteTextViewFrom.setCompoundDrawablesWithIntrinsicBounds(null, null,
                imgClearButton, null);
        mAutoCompleteTextViewFrom.setOnTouchListener(mAutoCompleteTextViewTouchListener);
        mAutoCompleteTextViewTo = (AutoCompleteTextView)
                findViewById(R.id.to_location);
        mPlaceAutoCompleteAdaperTo = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS_INDIA, 2, null);

        mAutoCompleteTextViewTo.setAdapter(mPlaceAutoCompleteAdaperTo);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutoCompleteTextViewTo.setOnItemClickListener(mAutocompleteClickListener);
//        Drawable imgClearButton = getResources().getDrawable(
//                R.drawable.abc_ic_clear_mtrl_alpha);


        mAutoCompleteTextViewTo.setCompoundDrawablesWithIntrinsicBounds(null, null,
                imgClearButton, null);
        mAutoCompleteTextViewTo.setOnTouchListener(mAutoCompleteTextViewTouchListener);
        mAPIResponseListView = (ListView) findViewById(R.id.api_response);
        mAPIResponseAdapter = new APIResponseAdapter(this, R.layout.api_response_item, mAPIResponseDataList);
        mAPIResponseListView.setAdapter(mAPIResponseAdapter);
        mAPIResponseListView.setOnItemClickListener(mAPIResponseListViewClickListener);

         mAutoCompleteViewFromTextWatcher = new AutoCompleteViewTextWatcher();
        mAutoCompleteViewToTextWatcher = new AutoCompleteViewTextWatcher();
         mAutoCompleteTextViewFrom.addTextChangedListener(mAutoCompleteViewFromTextWatcher);
        mAutoCompleteTextViewTo.addTextChangedListener(mAutoCompleteViewToTextWatcher);
        mRefreshButton = (ImageView) findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(mRefreshButtonClickListener);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        mRefreshButton.getLayoutParams().height = height/10;
        mRefreshButton.getLayoutParams().width = height/10;

//        InputMethodManager imm = (InputMethodManager)getSystemService(
//                Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(mAutoCompleteTextViewTo.getWindowToken(), 0);



        //mAutoCompleteTextViewFrom.addTextChangedListener(TextWatcher );

    }

    class AutoCompleteViewTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (this == mAutoCompleteViewFromTextWatcher) {
                    fromPlaceLat = null;
                    fromPlaceLng = null;
                    if(s.toString().equals("")) {
                        setCurrentLocation(true);
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

    public void loadETAAndFare(){
        if(fromPlaceLat != null && fromPlaceLng != null && toPlaceLng != null && toPlaceLat != null) {
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
            loadETAAndFare();
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
                    setCurrentLocation(true);
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

    public void setCurrentLocation(boolean isHint){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {


            List<Address> addresses = null;
            Address address = null;
            mGeocoder = new Geocoder(this, Locale.getDefault());
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            fromPlaceLat = Double.toString(latitude);
            fromPlaceLng = Double.toString(longitude);
            try {
                addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Canont get current Address!");
            }
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            StringBuilder fullAddr = new StringBuilder("");

            String fullAddr = "";
            if (!isHint && addresses != null) {
                address = addresses.get(0);
//                String primaryAddr = address.getAddressLine(0);
//                String subLocality = address.getSubLocality();
//                String city = address.getLocality();
//                fullAddr = primaryAddr + ", " + subLocality + ", " + city;
//                mAutoCompleteTextViewFrom.setText(fullAddr);
//                Request etaRequest = null;
//                Request priceRequest = null;
//                etaRequest = new Request();
//                etaRequest.setUrl(etaURL);
//                etaRequest.addParams("start_latitude", fromPlaceLat);
//                etaRequest.addParams("start_longitude", fromPlaceLng);
//                new HttpAsyncTask().execute(etaRequest, priceRequest);

                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    if(address.getAddressLine(i) != null) {
                        fullAddr += address.getAddressLine(i);
                    }
                    if(i !=  address.getMaxAddressLineIndex() - 1) {
                        fullAddr += ", ";
                    }
//                    fullAddr.append(address.getAddressLine(i)).append(",");
                }
                if(fullAddr == ""){
                    fromPlaceLat = null;
                    fromPlaceLng = null;
                }


                    mAutoCompleteTextViewFrom.setText(fullAddr);


            }
            else if (addresses != null) {
                mAutoCompleteTextViewFrom.setHint("Current Location");
               loadETAAndFare();
            }


        }
    }
    @Override
    public void onConnectionSuspended(int arg) {

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        if(mGoogleApiClient.isConnected()) {

            setCurrentLocation(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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
            }
           else {
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mToLocationCallback);

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

           fromPlaceLat = Double.toString(place.getLatLng().latitude);
           fromPlaceLng =  Double.toString(place.getLatLng().longitude);
            Request etaRequest = null;
            Request priceRequest = null;

            if(toPlaceLat != null && toPlaceLng != null) {
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

                new HttpAsyncTask().execute(etaRequest,priceRequest);

            }






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

            if(fromPlaceLat != null && fromPlaceLng != null) {
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


                new HttpAsyncTask().execute(etaRequest,priceRequest);
            }

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

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


        Log.e("TAG", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private class HttpAsyncTask extends AsyncTask<Request, Void, HashMap<RESPONSE_TYPE,String>> {
        @Override
        protected HashMap<RESPONSE_TYPE,String> doInBackground(Request... requests) {

//           mAPIResponseListView.
            ServiceHandler serviceHandler = new ServiceHandler();
            Request etaRequest = requests[0];
            Request priceRequest = requests[1];
            HashMap<RESPONSE_TYPE, String> responseMap = new HashMap<RESPONSE_TYPE, String>();
            responseMap.put(RESPONSE_TYPE.ETA, null);
            responseMap.put(RESPONSE_TYPE.PRICE, null);
            if(etaRequest != null) {
                String result = serviceHandler.makeServiceCall(etaRequest.getUrl(), 1, etaRequest.getRequestParams());
                responseMap.put(RESPONSE_TYPE.ETA, result);

            }
            if(priceRequest != null) {
                String result = serviceHandler.makeServiceCall(priceRequest.getUrl(), 1, priceRequest.getRequestParams());
                responseMap.put(RESPONSE_TYPE.PRICE, result);
            }

        return responseMap;


        }

        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this,ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Looking nearby cabs....");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(HashMap<RESPONSE_TYPE,String> responseMap) {
            dialog.dismiss();
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
                if(etaResponseStr != null) {
                    etaResponse = mapper.readValue(etaResponseStr, EtaResponse.class);
                }
                if(priceResponseStr != null) {
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

            if(etaResponse.getOla().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider(OLA);
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getOla().getDataList();

                APIResponseDataList = new ArrayList<APIResponseData>();
                if(priceResponse != null) {
                    priceDataList = priceResponse.getOla().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(OLA);
                    if(priceDataList != null) {
                    for(PriceData tempPriceData : priceDataList) {
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
            if(etaResponse.getUber().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider("Uber");
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getUber().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                if(priceResponse != null) {
                    priceDataList = priceResponse.getUber().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(UBER);
                    if(priceDataList != null) {
                        for(PriceData tempPriceData : priceDataList) {
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

            if(etaResponse.getTfs().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider("Taxi For Sure");
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                etaDataList = etaResponse.getTfs().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                if(priceResponse != null) {
                    priceDataList = priceResponse.getTfs().getDataList();
                }
                for (EtaData temp : etaDataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    apiResponseData.setCarProvider(TFS);
                    if(priceDataList != null) {
                        for(PriceData tempPriceData : priceDataList) {
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
            mRefreshButton.setVisibility(View.VISIBLE);

        }



    }
}
