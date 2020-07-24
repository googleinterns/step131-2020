package com.google.step;

import static java.lang.Math.toIntExact;

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.StringBuilder;

/**
 * This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore
 * corresponding to user form request. A POST request gets the parameter values from the form and
 * prepares the MapImages to be sent to QueryCloud.java.
 */
@WebServlet("/frontend-query-datastore")
public class FrontendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final Logger LOGGER = Logger.getLogger(FrontendQueryDatastore.class.getName());

    /** Get form parameters and query Datastore to get objectIDs based on those parameters */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Get parameters from form.
        ArrayList<String> zoomStrings = new ArrayList<>(Arrays.asList(request.getParameterValues("zoom-level")));
        ArrayList<String> cityStrings = new ArrayList<>(Arrays.asList(request.getParameterValues("city")));
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        // Add the appropriate filters according to the form input.
        CompositeFilter compositeFilter =
                buildCompositeFilter(zoomStrings, cityStrings, startDateStr, endDateStr);

        // Build the query for Datastore.
        Query query = new Query("MapImage").setFilter(compositeFilter);

        // Add all the mapEntities that matched the filter
        PreparedQuery resultList = datastore.prepare(query);
        ArrayList<MapImage> mapImages = new ArrayList<>();
        try {
            mapImages = entitiesToMapImages(resultList);
        } catch (DatastoreNeedIndexException e) {
            LOGGER.log(Level.WARNING, "Converting entities to MapImages: " + e.getMessage());
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
        try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        }
        con.getInputStream().close();
        response.sendRedirect("/app.html");
    }


    /**
     * * Builds a composite filter for the Datastore query. The Composite Filter is constructed by
     * first checking for empty values from the form, then using sub-filters of zooms, dates, and
     * locations based off user-input values from the form. *
     */
    private CompositeFilter buildCompositeFilter(
            ArrayList<String> zoomStrings, ArrayList<String> cityStrings, String startDateStr, String endDateStr) {
        // Most efficient filter ordering for Datastore query is equality, inequality, sort order.
        // For complex queries like these, an index must be made & deployed before building query.
        // Indexes must be made in WEB-INF/index.yaml. See index.yaml for more information.
        ArrayList<Filter> filters = new ArrayList<>();
        
        // Build city filters.
        if (!cityStrings.isEmpty()) {
            filters.add(buildCityFilters(cityStrings));
        }
        // Build zoom filters.
        if(!zoomStrings.isEmpty()) {
            filters.add(buildZoomFilters(zoomStrings));
        }
        // Build date filters.
        try {
            long startDateLong = Long.parseLong(startDateStr);
            long endDateLong = Long.parseLong(endDateStr);
            filters.add(buildDateFilters(startDateLong, endDateLong));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Building Date Filters: " + e.getMessage());
        }

        // Construct the CompositeFilter.
        CompositeFilter compositeFilter = null;
        if (filters.size() > 1) {
            compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        } else if (filters.size() == 1) {
            // Clone the filter to get around needing 2 sub-filters to construct a composite filter.
            filters.add(filters.get(0));
            compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        } else {
            // Load all MapImages from Datastore b/c all year properties are >= 2020.
            filters.add(FilterOperator.GREATER_THAN_OR_EQUAL.of("Year", 2020));
            filters.add(FilterOperator.GREATER_THAN_OR_EQUAL.of("Year", 2020));
            // NOTE: Sometimes all the images won't load the first time, but will the second time.
            compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        }
        return compositeFilter;
    }

    /** Helper function for buildCityFilters **/
    private Filter buildIndividualCityFilter(String city) {
        return FilterOperator.EQUAL.of("City Name", city);
    }

    /** Builds the city filters for the overall Composite Filter **/
    private Filter buildCityFilters(ArrayList<String> cityStrings) {
        ArrayList<Filter> cityFilters = new ArrayList<>();
        for (int i = 0; i < cityStrings.size(); i++) {
            String city = cityStrings.get(i);
            cityFilters.add(buildIndividualCityFilter(city));
        }
        if (cityFilters.size() > 1) {
            return new CompositeFilter(CompositeFilterOperator.OR, cityFilters);
        } else {
            // We ensure that cityFilters is not empty in buildCompositeFilter().
            return cityFilters.get(0);
        }
    }

    /** Helper function for buildZoomFilters **/
    private Filter buildIndividualZoomFilter(int zoom) {
        return FilterOperator.EQUAL.of("Zoom", zoom);
    }

    /** * Builds the zoom filters for the overall Composite Filter. * */
    private Filter buildZoomFilters(ArrayList<String> zoomStrings) {
        ArrayList<Filter> zoomFilters = new ArrayList<>();
        for (int i = 0; i < zoomStrings.size(); i++) {
            try{
                int zoom = Integer.parseInt(zoomStrings.get(i));
                zoomFilters.add(buildIndividualZoomFilter(zoom));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Building Zoom Filters: " + e.getMessage());
            }
        }
        if (zoomFilters.size() > 1) {
            return new CompositeFilter(CompositeFilterOperator.OR, zoomFilters);
        } else {
            // We ensure that zoomFilters is not empty in buildCompositeFilter().
            return zoomFilters.get(0);
        }
    }

    /** * Builds the date filters for the overall Composite Filter. * */
    private Filter buildDateFilters(long startDateLong, long endDateLong) {
        return new CompositeFilter(
                CompositeFilterOperator.AND,
                Arrays.asList(
                        FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", startDateLong),
                        FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", endDateLong)));
    }

    /**
     * * Converts the entities returned from the Datastore query into MapImage objects for us to
     * use. *
     */
    private ArrayList<MapImage> entitiesToMapImages(PreparedQuery resultList) {
        ArrayList<MapImage> resultMapImages = new ArrayList<>();
        for (Entity entity : resultList.asIterable()) {
            MapImage mapImage = entityToMapImage(entity);
            resultMapImages.add(mapImage);
        }
        return resultMapImages;
    }

    /*
     *   NOTE: entity.get"Type" (i.e. entity.getDouble) will return either DatastoreException
     *   if the property doesn't exist, or a ClassCastException if the value is the wrong type
     */
    /**
     * Helper function for entitiesToMapImages. Converts each individual entity into a MapImage
     * object.
     */
    private MapImage entityToMapImage(Entity entity) {
        double latitude = (double) entity.getProperty("Latitude");
        double longitude = (double) entity.getProperty("Longitude");
        long zoom = (long) entity.getProperty("Zoom");
        String cityName = (String) entity.getProperty("City Name");
        long month = (long) entity.getProperty("Month");
        long year = (long) entity.getProperty("Year");
        Long timeStamp = (long) entity.getProperty("Timestamp");
        MapImage mapImage =
                new MapImage(
                        longitude,
                        latitude,
                        cityName,
                        toIntExact(zoom),
                        toIntExact(month),
                        toIntExact(year),
                        timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
}
