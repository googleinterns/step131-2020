package com.google.step;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.Filter;
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
import java.util.logging.Logger;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;

/***
    This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore corresponding to user form request.
    A POST request gets the parameter values from the form and prepares the MapImages to be sent to query Google Cloud Storage.
***/
@WebServlet("/frontend-query-datastore")
public class FrontendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final static Logger LOGGER = Logger.getLogger(QueryCloud.class.getName());

    /** Get form parameters and query Datastore to get objectIDs based on those parameters*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); // Authorized Datastore service.
        KeyFactory keyFactory = datastore.newKeyFactory().setKind("MapImage"); // Used to create keys later.

        String zoomStr = request.getParameter("zoomLevel");
        String city = request.getParameter("city");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("yearInput");  

        // Add the appropriate filters according to the form input.
        CompositeFilter compositeFilter = buildCompositeFilter(zoomStr, city, monthStr, yearStr);

        // Build the query for Datastore.
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("MapImage")
            .setFilter(compositeFilter)
            .build();

        // Add all the mapEntities that matched the filter
        QueryResults<Entity> resultList = datastore.run(query);
        ArrayList<MapImage> mapImages = new ArrayList<>();
        try {
            mapImages = entitiesToMapImages(resultList);
        } catch (ClassCastException e) {
            // TODO: log error
        }

        // Send the MapImage metadata to QueryCloud.java
        Gson gson = new Gson();
        URL url = new URL("https://map-snapshot-step.uc.r.appspot.com/query-cloud");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        String data = gson.toJson(mapImages);
        try(DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        }
        con.getInputStream().close();
        response.sendRedirect("/app.html");
    }

    private CompositeFilter buildCompositeFilter(String zoomStr, String city, String monthStr, String yearStr) {
        // Check for empty values from the form and build filters for user-input values.
        ArrayList<Filter> filters = new ArrayList<>();
        try {
            // Zoom ranges are based on documented Zoom Bands.
            // Global zoom level (0-3) is not tracked.
            switch(zoomStr) {
                // Continental is zoom levels 4 - 6, but zoom level 4 is not tracked.
                case "Continental":
                    filters.addAll(buildZoomFilters(5, 6));
                    break;
                case "Regional":
                    filters.addAll(buildZoomFilters(7, 10));
                    break;
                case "Local":
                    filters.addAll(buildZoomFilters(11, 14));
                    break;
                case "Sublocal":
                    filters.addAll(buildZoomFilters(15, 16));
                    break;
                // House is zoom levels 17 - 20, but zoom levels 19-20 are not tracked.
                case "House":
                    filters.addAll(buildZoomFilters(17, 18));
                    break;
                default:
                    throw new IllegalArgumentException("Zoom not specified");
            }
        } catch (IllegalArgumentException e) {
            // TODO: log and handle error.
        }
        try {
            int month = Integer.parseInt(monthStr);
            filters.add(PropertyFilter.eq("Month", month));
        } catch (NumberFormatException e) {
            // TODO: log and handle error.
        }
        try {
            int year = Integer.parseInt(yearStr);
            filters.add(PropertyFilter.eq("Year", year));
        } catch (NumberFormatException e) {
            // TODO: log and handle error.
        }
        if (!city.equals("")) {
            filters.add(PropertyFilter.eq("City Name", city));
        }

        // Construct the CompositeFilter.
        CompositeFilter compositeFilter = null;
        if (filters.size() > 1) {
            // Filters.stream() allows us to pass an ArrayList to a function with VarArgs parameters
            compositeFilter = CompositeFilter.and(
                filters.get(0), filters.stream().skip(1).toArray(Filter[]::new));
        } else if (filters.size() == 1) {
            compositeFilter = CompositeFilter.and(filters.get(0));
        } else {
            // Load all MapImages from Datastore b/c all year properties are >= 2020
            compositeFilter = CompositeFilter.and(PropertyFilter.ge("Year", 2020));
        }
        return compositeFilter;
    }

    private ArrayList<Filter> buildZoomFilters(int startingZoom, int endingZoom) {
        ArrayList<Filter> zoomFilters = new ArrayList<>();
        for(int zoom = startingZoom; zoom <= endingZoom; zoom++) {
            zoomFilters.add(PropertyFilter.eq("Zoom", zoom));
        }
        return zoomFilters;
    }

    private ArrayList<MapImage> entitiesToMapImages(QueryResults<Entity> resultList) {
        ArrayList<MapImage> resultMapImages = new ArrayList<>();
        while (resultList.hasNext()) {  // While we still have data
            resultMapImages.add(entityToMapImage(resultList.next())); // Add the MapImage to the List
        }
        return resultMapImages;
    }

    private MapImage entityToMapImage(Entity entity) {
        /* 
        *   NOTE: entity.get"Type" (i.e. entity.getDouble) will return either DatastoreException
        *   if the property doesn't exist, or a ClassCastException if the value is the wrong type
        */
        double latitude = entity.getDouble("Latitude");
        double longitude = entity.getDouble("Longitude");
        int zoom = (int) entity.getLong("Zoom");
        String cityName = entity.getString("City Name");
        int month = (int) entity.getLong("Month");
        int year = (int) entity.getLong("Year");
        String timeStamp = entity.getString("Time Stamp");

        MapImage mapImage = new MapImage(longitude, latitude, cityName, zoom, month, year, timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
}