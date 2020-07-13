package com.google.step;

import com.google.appengine.api.datastore.Key;
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
@WebServlet("/form-locations")
public class FormLocations extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("I AM IN DOGET");
        // Query Datastore for the locations
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Store the MapImages from Datastore in a List. Grab location options for form selections.
        List<String> formLocationOptions = new ArrayList<>();
        System.out.println("RIGHT BEFORE FOR LOOP");
        for (Entity entity : results.asIterable()) {
            System.out.println("IN FOR LOOP");
            String cityName = (String) entity.getProperty("cityName");
            System.out.println("LOOK AT THIS CITY: " + cityName);
            formLocationOptions.add(cityName);
        }

        // // Store the MapImages from Datastore in a List. Grab location options for form selections.
        // List<MapImage> mapImages = new ArrayList<>();
        // for (Entity entity : results.asIterable()) {
        //     for (int zoom = 5; zoom <= 18; zoom++) {
        //         double latitude = (double) entity.getProperty("latitude");
        //         double longitude = (double) entity.getProperty("longitude");
        //         String cityName = (String) entity.getProperty("cityName");

        //         MapImage mapImage = new MapImage(latitude, longitude, zoom);
        //         mapImage.setCityName(cityName);
        //         mapImages.add(mapImage);
        //     }
        // }        

        Gson gson = new Gson();
        String jsonLocations = gson.toJson(formLocationOptions);
        response.setContentType("application/json");
        response.getWriter().println(jsonLocations);
    }
}