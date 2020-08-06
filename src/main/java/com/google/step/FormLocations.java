package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet retrieves tracked locations metadata and prepares it to be fetched and displayed as
 * location options in app.html form.
 */
@WebServlet("/form-locations")
public class FormLocations extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get metadata of tracked locations.
        PreparedQuery results = getQuery();

        // Add tracked locations as MapImage objects to a List.
        List<MapImage> formLocationOptions = convertMetdataToMapImages(results);

        Gson gson = new Gson();
        String jsonLocations = gson.toJson(formLocationOptions);
        response.setContentType("application/json");
        response.getWriter().println(jsonLocations);
    }

    /** * Query Datastore for the tracked location metadata. * */
    public PreparedQuery getQuery() {
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        return datastore.prepare(query);
    }

    /** * Make List of MapImages from tracked locations' metadata. * */
    public List<MapImage> convertMetdataToMapImages(PreparedQuery trackedMetadata) {
        List<MapImage> formLocationOptions = new ArrayList<>();
        for (Entity entity : trackedMetadata.asIterable()) {
            String location = (String) entity.getProperty("cityName");
            double latitude = (double) entity.getProperty("latitude");
            double longitude = (double) entity.getProperty("longitude");

            formLocationOptions.add(new MapImage(latitude, longitude, location));
        }
        return formLocationOptions;
    }
}
