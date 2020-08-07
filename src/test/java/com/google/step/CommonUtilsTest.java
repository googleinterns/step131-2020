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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.datastore.dev.LocalDatastoreService.AutoIdAllocationPolicy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

@RunWith(JUnit4.class)
public final class CommonUtilsTest {
    private DatastoreService datastore;

    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private final LocalDateTime JULY_9_2020 = LocalDateTime.of(2020, 7, 9, 6, 30);
    private final long JULY_9_2020_EPOCH = JULY_9_2020.toEpochSecond(ZoneOffset.UTC);
    private final LocalDateTime JULY_31_2020 = LocalDateTime.of(2020, 7, 31, 12, 30);
    private final long JULY_31_2020_EPOCH = JULY_31_2020.toEpochSecond(ZoneOffset.UTC);
    private final LocalDateTime AUGUST_5_2020 = LocalDateTime.of(2020, 8, 5, 18, 30);
    private final long AUGUST_5_2020_EPOCH = AUGUST_5_2020.toEpochSecond(ZoneOffset.UTC);

    MapImage MAPIMAGE_A = new MapImage(40.7128, -74.0060, "New York City", 6, 7, 2020, JULY_9_2020_EPOCH);
    MapImage MAPIMAGE_B = new MapImage(35.9128, -100.3821, "Canadian, TX", 12, 7, 2020, JULY_31_2020_EPOCH);
    MapImage MAPIMAGE_C = new MapImage(35.6907172, 139.7066884, "Tokyo", 18, 8, 2020, AUGUST_5_2020_EPOCH);
    
    Entity ENTITY_MAPIMAGE_A;
    Entity ENTITY_MAPIMAGE_B;
    Entity ENTITY_MAPIMAGE_C;

    @Before
    public void setUp() {
        //NOTE: Instantiating Entities before performing helper.setUp() causes the API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();

        // Instantiate MapImage Object IDs.
        MAPIMAGE_A.setObjectID();
        MAPIMAGE_B.setObjectID();
        MAPIMAGE_C.setObjectID();

        // Set up Datastore entities.
        ENTITY_MAPIMAGE_A = new Entity("MapImage");
        ENTITY_MAPIMAGE_A.setProperty("Latitude", 40.7128);
        ENTITY_MAPIMAGE_A.setProperty("Longitude", -74.0060);
        ENTITY_MAPIMAGE_A.setProperty("City Name", "New York City");
        ENTITY_MAPIMAGE_A.setProperty("Zoom", 6);
        ENTITY_MAPIMAGE_A.setProperty("Month", 7);
        ENTITY_MAPIMAGE_A.setProperty("Year", 2020);
        ENTITY_MAPIMAGE_A.setProperty("Timestamp", JULY_9_2020_EPOCH);
        ENTITY_MAPIMAGE_B = new Entity("MapImage");
        ENTITY_MAPIMAGE_B.setProperty("Latitude", 35.9128);
        ENTITY_MAPIMAGE_B.setProperty("Longitude", -100.3821);
        ENTITY_MAPIMAGE_B.setProperty("City Name", "Canadian, TX");
        ENTITY_MAPIMAGE_B.setProperty("Zoom", 12);
        ENTITY_MAPIMAGE_B.setProperty("Month", 7);
        ENTITY_MAPIMAGE_B.setProperty("Year", 2020);
        ENTITY_MAPIMAGE_B.setProperty("Timestamp", JULY_31_2020_EPOCH);
        ENTITY_MAPIMAGE_C = new Entity("MapImage");
        ENTITY_MAPIMAGE_C.setProperty("Latitude", 35.6907172);
        ENTITY_MAPIMAGE_C.setProperty("Longitude", 139.7066884);
        ENTITY_MAPIMAGE_C.setProperty("City Name", "Tokyo");
        ENTITY_MAPIMAGE_C.setProperty("Zoom", 18);
        ENTITY_MAPIMAGE_C.setProperty("Month", 8);
        ENTITY_MAPIMAGE_C.setProperty("Year", 2020);
        ENTITY_MAPIMAGE_C.setProperty("Timestamp", AUGUST_5_2020_EPOCH);
    }

    @After
    public void done() {
        helper.tearDown();
    }

    @Test
    public void entityToMapImage() {
        MapImage actual = CommonUtils.entityToMapImage(ENTITY_MAPIMAGE_A);
        Assert.assertEquals(MAPIMAGE_A, actual);
    }

    // TODO: Currently these methods work in production, but not the test environment.
    @Test
    public void entitiesToMapImages() {
        // Put the MapImage entities in Datastore.
        datastore.put(ENTITY_MAPIMAGE_A);
        datastore.put(ENTITY_MAPIMAGE_B);
        datastore.put(ENTITY_MAPIMAGE_C);

        // Get actual MapImages.
        Query query = new Query("MapImage").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery results = datastore.prepare(query); 
        ArrayList<MapImage> actual = CommonUtils.entitiesToMapImages(results);

        // Set up expected MapImages in alphabetical order.
        ArrayList<MapImage> expected = new ArrayList<>();
        expected.add(MAPIMAGE_B);
        expected.add(MAPIMAGE_A);
        expected.add(MAPIMAGE_C);

        Assert.assertEquals(expected, actual);
    }
}