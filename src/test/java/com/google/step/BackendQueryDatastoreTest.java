package com.google.step;

import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BackendQueryDatastoreTest {
    private DatastoreService datastore;
    private BackendQueryDatastore backendQueryDatastore;

    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    MapImage MAP_IMAGE_A = new MapImage(77.2176496, 28.6282961, "Delhi", 10, 7, 2020, 1594038988);
    MapImage MAP_IMAGE_B = new MapImage(-0.0911334, 51.5054466, "London", 7, 7, 2020, 1594038988);
    MapImage TRACKED_LOCATION_MAP_IMAGE_A = new MapImage(28.6282961, 77.2176496, "Delhi", 10);
    MapImage TRACKED_LOCATION_MAP_IMAGE_B = new MapImage(51.5054466, -0.0911334, "London", 7);
    Entity TRACKED_LOCATION_ENTITY_A;
    Entity TRACKED_LOCATION_ENTITY_B;

    @Before
    public void setUp() {
        // NOTE: Instantiating Entities before performing helper.setUp() causes the API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();

        // Set up Datastore entities.
        TRACKED_LOCATION_ENTITY_A = new Entity("TrackedLocation");
        TRACKED_LOCATION_ENTITY_B = new Entity("TrackedLocation");
        TRACKED_LOCATION_ENTITY_A.setProperty("latitude", 28.6282961);
        TRACKED_LOCATION_ENTITY_A.setProperty("longitude", 77.2176496);
        TRACKED_LOCATION_ENTITY_A.setProperty("cityName", "Delhi");
        TRACKED_LOCATION_ENTITY_B.setProperty("latitude", 51.5054466);
        TRACKED_LOCATION_ENTITY_B.setProperty("longitude", -0.0911334);
        TRACKED_LOCATION_ENTITY_B.setProperty("cityName", "London");

        backendQueryDatastore = new BackendQueryDatastore();
    }

    @After
    public void done() {
        helper.tearDown();
    }

    @Test
    public void getQuery_HasProperQuantity() {
        // Put the tracked locations into datastore.
        datastore.put(TRACKED_LOCATION_ENTITY_A);
        datastore.put(TRACKED_LOCATION_ENTITY_B);

        // Make sure datastore has the right # of entities.
        PreparedQuery actual = backendQueryDatastore.getQuery();
        Assert.assertEquals(2, actual.countEntities());
    }

    @Test
    public void loadProperAmountOfTrackedLocations() {
        // Put the tracked locations into datastore.
        datastore.put(TRACKED_LOCATION_ENTITY_A);
        datastore.put(TRACKED_LOCATION_ENTITY_B);

        // Load the tracked locations from Datastore.
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);
        List<MapImage> actual = backendQueryDatastore.loadTrackedLocations(results);

        // Add all tracked locations from start to end zooms in expected.
        List<MapImage> expected = new ArrayList<>();
        for (int zoom = 5; zoom <= 18; zoom++) {
            TRACKED_LOCATION_MAP_IMAGE_A.setZoom(zoom);
            TRACKED_LOCATION_MAP_IMAGE_B.setZoom(zoom);
            expected.add(TRACKED_LOCATION_MAP_IMAGE_A);
            expected.add(TRACKED_LOCATION_MAP_IMAGE_B);
        }

        Assert.assertEquals(expected.size(), actual.size());
    }

    @Test
    public void loadProperCityNames() {
        // Put the tracked locations into datastore.
        datastore.put(TRACKED_LOCATION_ENTITY_A);
        datastore.put(TRACKED_LOCATION_ENTITY_B);

        // Load the tracked locations from Datastore.
        Query query = new Query("TrackedLocation").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query);  
        List<MapImage> actual = backendQueryDatastore.loadTrackedLocations(results);

        // Add all tracked locations from start to end zooms in expected.
        List<MapImage> expected = new ArrayList<>();
        for (int zoom = 5; zoom <= 18; zoom++) {
            TRACKED_LOCATION_MAP_IMAGE_A.setZoom(zoom);
            TRACKED_LOCATION_MAP_IMAGE_B.setZoom(zoom);
            expected.add(TRACKED_LOCATION_MAP_IMAGE_A);
            expected.add(TRACKED_LOCATION_MAP_IMAGE_B);
        }

        // Get an element from the second set of tracked locations added.
        MapImage actualMapImage = actual.get(0);
        MapImage expectedMapImage = expected.get(0);

        Assert.assertEquals(expectedMapImage.getCityName(), actualMapImage.getCityName());
    }
}
