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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/***
    This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore.
    A GET request gets TrackedLocations so request URLs can be constructed and sent to Static Maps.
***/
@WebServlet("/query-datastore")
public class QueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Query Datastore for the locations and zoom levels that we need to get for this month.
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Store the MapImages from Datastore in a List.
        List<MapImage> mapImages = new ArrayList<>();
        for(Entity entity : results.asIterable()) {
            for(int zoom = 5; zoom <= 18; zoom++) {
                double latitude = (double) entity.getProperty("latitude");
                double longitude = (double) entity.getProperty("longitude");
                String cityName = (String) entity.getProperty("cityName");

                MapImage mapImage = new MapImage(latitude, longitude, zoom);
                mapImage.setCityName(cityName);
                mapImages.add(mapImage);
            }
        }

        // Send the location and zoom levels through JSON to SaveImages.java
        Gson gson = new Gson();
        URL url = new URL("https://map-snapshot-step.uc.r.appspot.com/save-images-job");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        String data = gson.toJson(mapImages);
        con.setRequestProperty("Content-Length", Integer.toString(data.length()));
        try(DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        }
        // TODO: add logging
        catch(IOException e) {

        }
        // Consume the InputStream
        new BufferedReader(new InputStreamReader(con.getInputStream()))
            .lines()
            .collect(Collectors.joining(""));
    }
}