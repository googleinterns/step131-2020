package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
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
    This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore.
    A GET request gets TrackedLocations so request URLs can be constructed and sent to Static Maps.
    A POST request gets the parameters from a form and returns the MapImages to be displayed.
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
            String cityName = (String) entity.getProperty("cityName");

            MapImage mapImage = new MapImage(latitude, longitude, zoom);
            mapImage.setCityName(cityName);
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

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /* TODO: Set up a way to use both the API and the package for Datastore.
        *   Currently, I cannot create a Filter object without using the Datastore package.
        *   However, using the package only and not the API causes too many headaches
        *   and makes the code unable to compile without a significant overhaul.
        *   The current idea is to use the API for doGet and the package for doPost.
        *   Future issues are converting the package "Entity" to the API "Entity".
        *   This causes the code to not compile. Currently the code does not compile.
        */

        String zoomStr = request.getParameter("Zoom Level");
        String city = request.getParameter("City");
        String monthStr = request.getParameter("Month");
        String yearStr = request.getParameter("yearInput");

        int zoom = null;
        int month = null;
        int year = null;    

        try {
            zoom = Integer.parseInt(zoomStr);
        } catch (NumberFormatException e) {
            // TODO: handle error
        }
        try {
            month = Integer.parseInt(monthStr);
        } catch (NumberFormatException e) {
            // TODO: handle error
        }
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            // TODO: handle error
        }

        // TODO: This code does not work when using the Datastore API. Only w/ the Datastore package
        ArrayList<Filter> filters = new ArrayList<>();
        CompositeFilter compositeFilter = null;
        if(zoom != null) {
            filters.add(PropertyFilter.eq("zoom", zoom));
        }
        if (month != null) {
            filters.add(PropertyFilter.eq("month", month));
        }
        if (year != null) {
            filters.add(PropertyFilter.eq("year", year));
        }
        if(!city.equals("")) {
            filters.add(PropertyFilter.eq("cityName", city));
        }

        for(Filter filter : filters) {
            compositeFilter = CompositeFilter.and(filter);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("mapImage")
            .setFilter(compositeFilter)
            .build();

        // Add all the mapEntities that matched the filter
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);
        ArrayList<MapImage> mapImages = new ArrayList<>();
        for(Entity entity : results.asIterable()) {
            double latitude = (double) entity.getProperty("latitude");
            double longitude = (double) entity.getProperty("longitude");
            int zoom = (int) entity.getProperty("zoom");
            String cityName = (String) entity.getProperty("cityName");
            int month = (int) entity.getProperty("month");
            int year = (int) entity.getProperty("year");
            String timeStamp = (String) entity.getProperty("timeStamp");

            MapImage mapImage = new MapImage(longitude, latitude, cityName, zoom, month, year, timeStamp);
            mapImages.add(mapImage);
        }

        // Send the mapImages back to app.html
        Gson gson = new Gson();
        URL url = new URL("/app.html");
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