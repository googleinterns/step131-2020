package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreNeedIndexException;

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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Calendar.Builder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.lang.Math.toIntExact;


/***
    This servlet retrieves the mapImage metadata (location, zoom level, etc.) from Datastore 
    corresponding to user form request. A POST request gets the parameter values from the form 
    and prepares the MapImages to be sent to QueryCloud.java.
***/
@WebServlet("/frontend-query-datastore")
public class FrontendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final Logger LOGGER = Logger.getLogger(FrontendQueryDatastore.class.getName());

    /** Get form parameters and query Datastore to get objectIDs based on those parameters*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String zoomStr = request.getParameter("zoomLevel");
        String city = request.getParameter("city");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");  

        // Add the appropriate filters according to the form input.
        CompositeFilter compositeFilter = buildCompositeFilter(zoomStr, city, startDateStr, endDateStr);

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
        try(DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        }
        con.getInputStream().close();
        response.sendRedirect("/app.html");
    }

    /***
        Builds a composite filter for the Datastore query. The Composite Filter is constructed by
        first checking for empty values from the form, then using sub-filters of zooms, dates, 
        and locations based off user-input values from the form.
    ***/
    private CompositeFilter buildCompositeFilter(String zoomStr, String city, String startDateStr, String endDateStr) {
        // Most efficient filter ordering for Datastore query is equality, inequality, sort order.
        // For complex queries like these, an index must be made and deployed prior to building the query.
        // Indexes must be made in WEB-INF/index.yaml. See index.yaml for more information.
        ArrayList<Filter> filters = new ArrayList<>();
        if (!city.equals("")) {
            filters.add(FilterOperator.EQUAL.of("City Name", city));
        }
        try {
            // Zoom ranges are based on documented Zoom Bands.
            // Global zoom level (0-3) is not tracked.
            switch(zoomStr) {
                // Continental is zoom levels 4 - 6, but zoom level 4 is not tracked.
                case "Continental":
                    filters.add(buildZoomFilters(5, 6));
                    break;
                case "Regional":
                    filters.add(buildZoomFilters(7, 10));
                    break;
                case "Local":
                    filters.add(buildZoomFilters(11, 14));
                    break;
                case "Sublocal":
                    filters.add(buildZoomFilters(15, 16));
                    break;
                // House is zoom levels 17 - 20, but zoom levels 19-20 are not tracked.
                case "House":
                    filters.add(buildZoomFilters(17, 18));
                    break;
                default:
                    throw new IllegalArgumentException("Zoom not specified");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Building Zoom Filters: " + e.getMessage());
        }
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
        } else if(filters.size() == 1) {
            // Clone the filter to get around needing 2 sub-filters to construct a composite filter.
            filters.add(filters.get(0));
            compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        } else {
            // Load all MapImages from Datastore b/c all year properties are >= 2020.
            filters.add(FilterOperator.GREATER_THAN_OR_EQUAL.of("Year", 2020));
            filters.add(FilterOperator.GREATER_THAN_OR_EQUAL.of("Year", 2020));
            compositeFilter = new CompositeFilter(CompositeFilterOperator.AND, filters);
        }
        return compositeFilter;
    }

    /***
        Builds the zoom filters for the overall Composite Filter.
    ***/
    private Filter buildZoomFilters(int startingZoom, int endingZoom) {
        ArrayList<Filter> zoomFilters = new ArrayList<>();
        for(int zoom = startingZoom; zoom <= endingZoom; zoom++) {
            zoomFilters.add(FilterOperator.EQUAL.of("Zoom", zoom));
        }
        return new CompositeFilter(CompositeFilterOperator.OR, zoomFilters);
    }

    /***
        Builds the date filters for the overall Composite Filter.
    ***/
    private Filter buildDateFilters(long startDateLong, long endDateLong) {
        return new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(
            FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", startDateLong),
            FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", endDateLong)));
    }

    /***
        Converts the entities returned from the Datastore query into MapImage objects for us to use.
    ***/
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
    /***
        Helper function for entitiesToMapImages.
        Converts each individual entity into a MapImage object.
    ***/
    private MapImage entityToMapImage(Entity entity) { 
        double latitude = (double) entity.getProperty("Latitude");
        double longitude = (double) entity.getProperty("Longitude");
        long zoom = (long) entity.getProperty("Zoom");
        String cityName = (String) entity.getProperty("City Name");
        long month = (long) entity.getProperty("Month");
        long year = (long) entity.getProperty("Year");
        Long timeStamp = (long) entity.getProperty("Timestamp");
        MapImage mapImage = new MapImage(longitude, latitude, cityName, 
            toIntExact(zoom), toIntExact(month), toIntExact(year), timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
}
