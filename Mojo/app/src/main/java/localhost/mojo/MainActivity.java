package localhost.mojo;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static String etaURL = "https://fierce-refuge-4051.herokuapp.com/eta";
    protected GoogleApiClient mGoogleApiClient;
    private  PlaceAutoCompleteAdapter mPlaceAutoCompleteAdaper;
    private AutoCompleteTextView mAutoCompleteTextView;
    private APIResponseAdapter mApiResponseAdapter;
    private  String TAG = "MainActivity";
    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(8.0689, 77.5523), new LatLng(33.24902, 76.82704) );

    private  List<APIResponseData> mAPIResponseDataList  = new ArrayList<APIResponseData>();
    private  ListView mAPIResponseListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .build();

        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutoCompleteTextView = (AutoCompleteTextView)
                findViewById(R.id.from_location);
        mPlaceAutoCompleteAdaper = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,mGoogleApiClient,BOUNDS_INDIA,null);
        mAutoCompleteTextView.setAdapter(mPlaceAutoCompleteAdaper);
        // Register a listener that receives callbacks when a suggestion has been selected
        mAutoCompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mAPIResponseListView = (ListView) findViewById(R.id.api_response);
        mApiResponseAdapter = new APIResponseAdapter(this, R.layout.api_response_item,mAPIResponseDataList);
        mAPIResponseListView.setAdapter(mApiResponseAdapter);

    }

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
            final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mPlaceAutoCompleteAdaper.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);


            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

//            Toast.makeText(getApplicationContext(), "Clicked: " + item.description,
//                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
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

            String placeLat = Double.toString(place.getLatLng().latitude);
            String placeLng =  Double.toString(place.getLatLng().longitude);

            Request request = new Request();
            request.setUrl(etaURL);
            request.addParams("start_latitude", placeLat);
            request.addParams("start_longitude", placeLng);

            new HttpAsyncTask().execute(request);

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
    private class HttpAsyncTask extends AsyncTask<Request, Void, String> {
        @Override
        protected String doInBackground(Request... requests) {

            ServiceHandler serviceHandler = new ServiceHandler();
            Request request = requests[0];
            return serviceHandler.makeServiceCall(request.getUrl(), 1, request.getRequestParams());

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            ObjectMapper mapper = new ObjectMapper();
            EtaResponse etaResponse = null;
            try {
                System.out.println(result);
                etaResponse = mapper.readValue(result, EtaResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            MainActivity.this.mAPIResponseDataList.clear();
            APIResponseData headerData = null;
            List<EtaData> dataList;
            List<APIResponseData> APIResponseDataList;
            APIResponseData apiResponseData;

            if(etaResponse.getOla().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider("Ola");
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                dataList = etaResponse.getOla().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                for (EtaData temp : dataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    APIResponseDataList.add(apiResponseData);
                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }
            if(etaResponse.getUber().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider("Uber");
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                dataList = etaResponse.getUber().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                for (EtaData temp : dataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    APIResponseDataList.add(apiResponseData);
                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }

            if(etaResponse.getTfs().getDataList().size() != 0) {
                headerData = new APIResponseData();
                headerData.setCarProvider("Taxi For Sure");
                headerData.setEtaData(null);
                MainActivity.this.mAPIResponseDataList.add(headerData);

                dataList = etaResponse.getTfs().getDataList();
                APIResponseDataList = new ArrayList<APIResponseData>();
                for (EtaData temp : dataList) {
                    apiResponseData = new APIResponseData();
                    apiResponseData.setEtaData(temp);
                    APIResponseDataList.add(apiResponseData);
                }
                MainActivity.this.mAPIResponseDataList.addAll(APIResponseDataList);
            }
            mApiResponseAdapter.notifyDataSetChanged();


        }



    }
}
