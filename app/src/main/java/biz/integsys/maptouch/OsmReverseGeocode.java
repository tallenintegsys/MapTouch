package biz.integsys.maptouch;

import android.location.Location;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class OsmReverseGeocode {
    final String MAPQUEST_KEY = "IckskFhc8BIrs9MXKxZA9GtktXEznKxB";
    private final String GEOCODE_BASE_URI = "http://nominatim.openstreetmap.org/reverse?";
    //private final String GEOCODE_BASE_URI = "http://open.mapquestapi.com/nominatim/v1/reverse.php?key=" + MAPQUEST_KEY;
    class Result {
        public Location location; //actual location snapped to
        public String country = "";
        public String state = "";
        public String county = "";
        public String city = "";
        public String town = ""; //used for testing only
        public String village = ""; //used for testing only
        public String streetName = "";
        public String streetNumber = "";
        public String zipcode = "";
    }

    public Result reverseGeocode(Location location) {
        Result result = new Result();
        final int timeout = 3000;

        try {
            String geocodeFeed = GEOCODE_BASE_URI + "&format=xml";
            geocodeFeed += "&lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
            geocodeFeed += "&zoom=18&addressdetails=1";
            URL url = new URL(geocodeFeed);
            // Open the connection
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.addRequestProperty("User-Agent", "NearbyExplorer");
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode(); //SYN here
            if (responseCode != HttpURLConnection.HTTP_OK) return null;

            // Use the XML Pull Parser to extract address
            InputStream in = httpConnection.getInputStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, null);
            int eventType;
            boolean ap = false;
            while ((eventType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("result")) {
                        result.location = new Location("OSM");
                        result.location.setLatitude(Double.valueOf(xpp.getAttributeValue(null, "lat")));
                        result.location.setLongitude(Double.valueOf(xpp.getAttributeValue(null, "lon")));
                    }

                    if (xpp.getName().equals("addressparts"))
                        ap = true;
                    if (ap && xpp.getName().equals("country"))
                        result.country = xpp.nextText();
                    if (ap && xpp.getName().equals("state"))
                        result.state = xpp.nextText();
                    if (ap && xpp.getName().equals("county"))
                        result.county = xpp.nextText();
                    if (ap && xpp.getName().equals("city"))
                        result.city = xpp.nextText();
                    if (ap && xpp.getName().equals("town"))
                        result.town = xpp.nextText();
                    if (ap && xpp.getName().equals("village"))
                        result.village = xpp.nextText();
                    if (ap && xpp.getName().equals("road"))
                        result.streetName = xpp.nextText();
                    if (ap && xpp.getName().equals("house_number"))
                        result.streetNumber = xpp.nextText();
                    if (ap && xpp.getName().equals("postcode"))
                        result.zipcode = xpp.nextText();
                }

                if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equals("address_component"))
                        ap = false;
                }
            }//while
        } catch (Exception e) {
           return null;
        }

        return result;
    }

}
