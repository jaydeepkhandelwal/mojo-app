package localhost.mojo;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

/**
 * Created by jaydeep on 02/07/15.
 */
public class Map extends Activity  {

    private GoogleMap googleMap = null;

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
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
}
