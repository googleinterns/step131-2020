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
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;

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
        Query query = new Query("trackedLocations").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Store the MapImages from Datastore in a List.
        List<MapImage> mapImages = new ArrayList<>();
        for(Entity entity : results.asIterable()) {
            double latitude = (double) entity.getProperty("latitude");
            double longitude = (double) entity.getProperty("longitude");
            int zoom = (int) entity.getProperty("zoom");

            MapImage mapImage = new MapImage(latitude, longitude, zoom);
            mapImages.add(mapImage);
        }

        // Send the location and zoom levels through JSON to SaveImages.java
        Gson gson = new Gson();
        URL url = new URL("/save-images-job");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String data = gson.toJson(mapImages);
        try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }
}