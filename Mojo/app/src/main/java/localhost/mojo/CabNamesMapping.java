package localhost.mojo;

import java.util.HashMap;

/**
 * Created by jaydeep on 03/07/15.
 */
public class CabNamesMapping {
    private static HashMap<String,String> cabNamesMap;
    public static void init(){
        cabNamesMap = new HashMap<String,String>();
        cabNamesMap.put("economy_sedan","Sedan");
        cabNamesMap.put("compact", "Mini");
        cabNamesMap.put("luxury_sedan", "Luxury");
        cabNamesMap.put("hatchback", "Hatchback");
        cabNamesMap.put("sedan","Sedan");
        cabNamesMap.put("uberX", "Uber X");
        cabNamesMap.put("UberBLACK", "Uber Black");
        cabNamesMap.put("nano", "Nano");



    }
    public static HashMap<String,String> getCabNamesMap(){
        return  cabNamesMap;
    }

}
