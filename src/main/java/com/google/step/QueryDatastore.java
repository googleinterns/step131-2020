package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
    This servlet retrieves the mapImage metadata (location, zoom level, etc.)
    from Datastore so request URLs can be constructed and sent to Static Maps.
***/
@WebServlet("/query-datastore")
public class QueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Query Datastore for the locations and zoom levels that we need to get for this month.
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("mapImage").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Store the MapImages from Datastore in a List.
        List<Comment> mapImages = new ArrayList<>();
        for(Entity entity : results.asIterable()) {
            double latitude = (double) entity.getProperty("latitude");
            double longitude = (double) entity.getProperty("longitude");
            int zoom = (int) entity.getProperty("zoom");

            MapImage mapImage = new MapImage(latitude, longitude, zoom);
            mapImages.add(mapImage);
        }

        // Send the location and zooms through JSON to the Static Maps servlet.
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.getWriter().println(gson.toJson(mapImages));
        response.sendRedirect("/query-static-maps")

    }
}