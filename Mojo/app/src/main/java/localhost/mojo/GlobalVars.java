package localhost.mojo;

import android.location.Geocoder;
import android.location.Location;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaydeep on 02/07/15.
 */
public class GlobalVars {

    public final static String etaURL = "https://fierce-refuge-4051.herokuapp.com/eta";
    public final static String priceURL = "https://fierce-refuge-4051.herokuapp.com/price";
    public static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(8.0689, 77.5523), new LatLng(33.24902, 76.82704));



    public final static String OLA = "Ola";
    public final static String UBER = "Uber";
    public final static String TFS = "Tfs";


}
