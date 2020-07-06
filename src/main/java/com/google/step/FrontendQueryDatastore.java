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

/***
    This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore.
    A POST request gets the parameters from a form and returns the MapImages to be displayed.
***/
@WebServlet("/frontend-query-datastore")
public class FrontendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    /** Get form parameters and query Datastore to get objectIDs based on those parameters*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService(); // Authorized Datastore service.
        KeyFactory keyFactory = datastore.newKeyFactory().setKind("mapImage"); // Used to create keys later.

        String zoomStr = request.getParameter("zoomLevel");
        String city = request.getParameter("city");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("yearInput");  

        // Add the appropriate filters according to the form input.
        CompositeFilter compositeFilter = buildCompositeFilter(zoomStr, city, monthStr, yearStr);

        // Build the query for Datastore.
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("mapImage")
            .setFilter(compositeFilter)
            .build();

        // Add all the mapEntities that matched the filter
        QueryResults<Entity> resultList = datastore.run(query);
        ArrayList<MapImage> mapImages = entitiesToMapImages(resultList);

        // Send the mapImages back to app.html
        Gson gson = new Gson();
        URL url = new URL("/app.html");
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
    }

    private CompositeFilter buildCompositeFilter(String zoomStr, String city, String monthStr, String yearStr) {
        // Check for empty values from the form and build filters for user-input values.
        ArrayList<Filter> filters = new ArrayList<>();
        try {
            int zoom = Integer.parseInt(zoomStr);
            filters.add(PropertyFilter.eq("zoom", zoom));
        } catch (NumberFormatException e) {
            // TODO: log and handle error.
        }
        try {
            int month = Integer.parseInt(monthStr);
            filters.add(PropertyFilter.eq("month", month));
        } catch (NumberFormatException e) {
            // TODO: log and handle error.
        }
        try {
            int year = Integer.parseInt(yearStr);
            filters.add(PropertyFilter.eq("year", year));
        } catch (NumberFormatException e) {
            // TODO: log and handle error.
        }
        if (!city.equals("")) {
            filters.add(PropertyFilter.eq("cityName", city));
        }

        // Construct the CompositeFilter.
        CompositeFilter compositeFilter = null;
        for (Filter filter : filters) {
            compositeFilter = CompositeFilter.and(filter);
        }
        return compositeFilter;
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

        double latitude = entity.getDouble("latitude");
        double longitude = entity.getDouble("longitude");
        int zoom = (int) entity.getLong("zoom");
        String cityName = entity.getString("cityName");
        int month = (int) entity.getLong("month");
        int year = (int) entity.getLong("year");
        String timeStamp = entity.getString("timeStamp");

        MapImage mapImage = new MapImage(longitude, latitude, cityName, zoom, month, year, timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
}