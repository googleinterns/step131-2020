package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet retrieves the tracked location metadata (location and coordinates) from Datastore.
 * This metadata along with a range of zoom levels is used to instantiate new MapImage instances
 * representing new map snapshots. The MapImage instances are sent to SaveImageCloud.java.
 */
@WebServlet(
        name = "BackendQueryDatastore",
        description = "taskqueue: Get MapImage data from Datastore",
        urlPatterns = "/backend-query-datastore")
public class BackendQueryDatastore extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Query Datastore for the locations and zoom levels that we need to get for this month.
        PreparedQuery results = getQuery();

        // Combine Datastore tracked metadata with zooms to store new MapImage objects in a List.
        List<MapImage> mapImages = loadTrackedLocations(results);

        // Send new MapImage objects through JSON to SaveImageCloud.java
        Gson gson = new Gson();
        String data = gson.toJson(mapImages);
        Queue queue = QueueFactory.getDefaultQueue();
        TaskOptions options =
                TaskOptions.Builder.withUrl("/save-images-cloud-job")
                        .method(TaskOptions.Method.POST)
                        .payload(data.getBytes(), "application/json");
        queue.add(options);
    }

    /** * Query Datastore for the locations and zoom levels that we need to get for the month. * */
    public PreparedQuery getQuery() {
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        return datastore.prepare(query);
    }

    /** Utilize Datastore tracked metadata with zooms to create new MapImage objects in a List. * */
    public List<MapImage> loadTrackedLocations(PreparedQuery results) {
        List<MapImage> mapImages = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            for (int zoom = 5; zoom <= 18; zoom++) {
                double latitude = (double) entity.getProperty("latitude");
                double longitude = (double) entity.getProperty("longitude");
                String cityName = (String) entity.getProperty("cityName");

                MapImage mapImage = new MapImage(latitude, longitude, cityName, zoom);
                mapImages.add(mapImage);
            }
        }
        return mapImages;
    }
}
