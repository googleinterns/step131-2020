package com.google.step;

import static java.lang.Math.toIntExact;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import java.util.ArrayList;

public class CommonUtils {
    /** * Converts entities returned from a Datastore query into MapImage objects to use. * */
    public static ArrayList<MapImage> entitiesToMapImages(PreparedQuery resultList) {
        ArrayList<MapImage> resultMapImages = new ArrayList<>();
        for (Entity entity : resultList.asIterable()) {
            MapImage mapImage = entityToMapImage(entity);
            resultMapImages.add(mapImage);
        }
        return resultMapImages;
    }

    /**
     * Helper function for entitiesToMapImages(PreparedQuery pq). Converts each individual entity
     * into a MapImage object. NOTE: entity.get"Type" (i.e. entity.getDouble) will return either
     * DatastoreException if the property doesn't exist, or a ClassCastException if the value is the
     * wrong type.
     */
    public static MapImage entityToMapImage(Entity entity) {
        double latitude = (double) entity.getProperty("Latitude");
        double longitude = (double) entity.getProperty("Longitude");
        long zoom = (long) entity.getProperty("Zoom");
        String cityName = (String) entity.getProperty("City Name");
        long month = (long) entity.getProperty("Month");
        long year = (long) entity.getProperty("Year");
        long timeStamp = (long) entity.getProperty("Timestamp");
        MapImage mapImage =
                new MapImage(
                        longitude,
                        latitude,
                        cityName,
                        toIntExact(zoom),
                        toIntExact(month),
                        toIntExact(year),
                        timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
}
