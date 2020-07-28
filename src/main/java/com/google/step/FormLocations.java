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
 * This servlet retrieves the tracked locations to display as location options in the app.html form.
 */
@WebServlet("/form-locations")
public class FormLocations extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Query Datastore for tracked metadata
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Add tracked locations to a List.
        List<MapImage> formLocationOptions = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            String location = (String) entity.getProperty("cityName");
            double latitude = (double) entity.getProperty("latitude");
            double longitude = (double) entity.getProperty("longitude");

            formLocationOptions.add(new MapImage(location, latitude, longitude));
        }

        Gson gson = new Gson();
        String jsonLocations = gson.toJson(formLocationOptions);
        response.setContentType("application/json");
        response.getWriter().println(jsonLocations);
    }
}
