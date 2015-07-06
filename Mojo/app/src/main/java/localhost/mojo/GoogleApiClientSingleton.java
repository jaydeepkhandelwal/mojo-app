package localhost.mojo;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

/**
 * Created by jaydeep on 01/07/15.
 */




public class GoogleApiClientSingleton extends Application {

    private static GoogleApiClient mGoogleApiClient;
    public Context mContext;
    private final static String TAG = "GoogleApiClientSingleton";
   // private static ProgressDialog dialog = null;
    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = getApplicationContext();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
//                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
       // dialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

//    static void Init(GoogleApiClient googleApiClient) {
//        if(mGoogleApiClient == null) {
//            mGoogleApiClient = googleApiClient;
//        }
//    }

    public static GoogleApiClient getGoogleApiClient() {
       return mGoogleApiClient;
  }
//
//    public static ProgressDialog getDialog() {
//        return dialog;
//    }



}
