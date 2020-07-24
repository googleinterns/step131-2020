package com.google.step;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;

import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.datastore.dev.LocalDatastoreService.AutoIdAllocationPolicy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

@RunWith(JUnit4.class)
public final class BackendQueryDatastoreTest {
    private DatastoreService datastore;
    //private BackendQueryDatastore backendQueryDatastore;

    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

    MapImage MAP_IMAGE_A = new MapImage(77.2176496, 28.6282961, "Delhi", 10, 7, 2020, 1594038988);
    MapImage MAP_IMAGE_B = new MapImage(-0.0911334, 51.5054466, "London", 7, 7, 2020, 1594038988);
    Entity TRACKED_LOCATION_ENTITY_A = new Entity("TrackedLocation");
    Entity TRACKED_LOCATION_ENTITY_B = new Entity("TrackedLocation");

    @Before
    public void setUp() {
        //TODO: might have to run the datastore emulator from cmd before running tests.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();
        MAP_IMAGE_A = new MapImage(77.2176496, 28.6282961, "Delhi", 10, 7, 2020, 1594038988);
        MAP_IMAGE_B = new MapImage(-0.0911334, 51.5054466, "London", 7, 7, 2020, 1594038988);
        Entity TRACKED_LOCATION_ENTITY_A = new Entity("TrackedLocation");
        Entity TRACKED_LOCATION_ENTITY_B = new Entity("TrackedLocation");
        //backendQueryDatastore = new BackendQueryDatastore();
    }

    @After
    public void done() {
        helper.tearDown();
    }

    @Test
    public void getQuery_HasProperQuantity() {
        // Set up Datastore entities.
        //DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       /* TRACKED_LOCATION_ENTITY_A.setProperty("latitude", 28.6282961);
        TRACKED_LOCATION_ENTITY_A.setProperty("longitude", 77.2176496);
        TRACKED_LOCATION_ENTITY_A.setProperty("cityName", "Delhi");
        TRACKED_LOCATION_ENTITY_B.setProperty("latitude", 51.5054466);
        TRACKED_LOCATION_ENTITY_B.setProperty("longitude", -0.0911334);
        TRACKED_LOCATION_ENTITY_B.setProperty("cityName", "London");
        datastore.put(TRACKED_LOCATION_ENTITY_A);
        datastore.put(TRACKED_LOCATION_ENTITY_B);

        PreparedQuery actual = backendQueryDatastore.getQuery();
        Assert.assertEquals(2, actual.countEntities());*/
    }

    @Test
    public void loadTrackedLocations() {
        //DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        /*PreparedQuery results = datastore.prepare(new Query("trackedLocations"));
        List<MapImage> actual = backendQueryDatastore.loadTrackedLocations(results);
        List<MapImage> expected = new ArrayList<>();
        expected.add(MAP_IMAGE_A);
        expected.add(MAP_IMAGE_B);

        Assert.assertEquals(expected, actual);
        System.out.println("yo");*/
    }
}