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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/*** 
    This servlet retrieves the tracked metadata (location and coordinates) from Datastore.
    This metadata along with a range of zoom levels is used to initiate new MapImage instances representing new map snapshots.
    The MapImage instances are sent to SaveImageCloud.java
    A GET request gets TrackedLocations to instantiate new MapImages.
***/
@WebServlet(
    name = "BackendQueryDatastore",
    description = "taskqueue: ",
    urlPatterns = "/backend-query-datastore"
    )
public class BackendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Query Datastore for the locations and zoom levels that we need to get for this month.
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);

        // Combine Datastore tracked metadata with zooms to store new MapImage objects in a List.
        List<MapImage> mapImages = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            for (int zoom = 5; zoom <= 18; zoom++) {
                double latitude = (double) entity.getProperty("latitude");
                double longitude = (double) entity.getProperty("longitude");
                String cityName = (String) entity.getProperty("cityName");

                MapImage mapImage = new MapImage(latitude, longitude, zoom);
                mapImage.setCityName(cityName);
                mapImages.add(mapImage);
            }
        }
        // Send new MapImage objects through JSON to SaveImageCloud.java
        Gson gson = new Gson();
        String data = gson.toJson(mapImages);
        Queue queue = QueueFactory.getDefaultQueue();
        TaskOptions options = TaskOptions.Builder.withUrl("/save-images-cloud-job")
            .method(TaskOptions.Method.POST)
            .payload(data.getBytes(), "application/json");
        queue.add(options);
    }
}