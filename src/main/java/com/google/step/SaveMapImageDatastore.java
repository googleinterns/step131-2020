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
 * This servlet retrieves MapImages after many attributes are set in SaveImageCloud.java to be
 * stored in Datastore.
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

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (MapImage mapImage : mapImages) {
            Entity mapImageEntity = createEntity(mapImage, "MapImage");
            Entity driveMapImageEntity = createEntity(mapImage, "DriveMapImage");
            datastore.put(mapImageEntity);
            datastore.put(driveMapImageEntity);
        }
    }

    /** Makes new entity of kind entityKind and sets properties. */
    public Entity createEntity(MapImage mapImage, String entityKind) {
            // An entity's look-up key is its objectID.
            Entity entity = new Entity(entityKind, mapImage.getObjectID());
            entity.setProperty("Latitude", mapImage.getLatitude());
            entity.setProperty("Longitude", mapImage.getLongitude());
            entity.setProperty("City Name", mapImage.getCityName());
            entity.setProperty("Zoom", mapImage.getZoom());
            entity.setProperty("Month", mapImage.getMonth());
            entity.setProperty("Year", mapImage.getYear());
            entity.setProperty("Timestamp", mapImage.getTimeStamp());
        return entity;
    }
}
