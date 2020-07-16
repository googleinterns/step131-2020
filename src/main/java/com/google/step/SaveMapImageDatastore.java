package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * * This servlet retrieves MapImages after many attributes are set in SaveImageCloud.java to be
 * stored in Datastore. A POST request gets the MapImage ArrayList to store each MapImage instance
 * in Datastore. *
 */
@WebServlet("/save-mapimage-datastore")
public class SaveMapImageDatastore extends HttpServlet {

    /** Stores the updated MapImage objects' metadata into Datastore. */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Get  MapImage objects here from SaveImageCloud.java
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        ArrayList<MapImage> mapImages =
                gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>() {}.getType());

        // Put updated MapImages into Datastore under the entity kind 'MapImage'. An entity's
        // look-up key is its objectID.
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (MapImage mapImage : mapImages) {
            Entity mapImageEntity = new Entity("MapImage", mapImage.getObjectID());
            mapImageEntity.setProperty("Longitude", mapImage.getLongitude());
            mapImageEntity.setProperty("Latitude", mapImage.getLatitude());
            mapImageEntity.setProperty("City Name", mapImage.getCityName());
            mapImageEntity.setProperty("Zoom", mapImage.getZoom());
            mapImageEntity.setProperty("Month", mapImage.getMonth());
            mapImageEntity.setProperty("Year", mapImage.getYear());
            mapImageEntity.setProperty("Time Stamp", mapImage.getTimeStamp());

            Entity driveMapImageEntity = new Entity("DriveMapImage", mapImage.getObjectID());
            driveMapImageEntity.setProperty("Longitude", mapImage.getLongitude());
            driveMapImageEntity.setProperty("Latitude", mapImage.getLatitude());
            driveMapImageEntity.setProperty("City Name", mapImage.getCityName());
            driveMapImageEntity.setProperty("Zoom", mapImage.getZoom());
            driveMapImageEntity.setProperty("Month", mapImage.getMonth());
            driveMapImageEntity.setProperty("Year", mapImage.getYear());
            driveMapImageEntity.setProperty("Time Stamp", mapImage.getTimeStamp());

            datastore.put(mapImageEntity);
            datastore.put(driveMapImageEntity);
        }
    }
}
