package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.datastore.dev.LocalDatastoreService.AutoIdAllocationPolicy;
import java.lang.Integer;
import static java.lang.Math.toIntExact;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SaveMapImageDatastoreTest {
    private DatastoreService datastore;
    private SaveMapImageDatastore SaveMapImageDatastore;

    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private MapImage MAPIMAGE_A;
    private MapImage MAPIMAGE_B;
    private Entity RESULT_ENTITY_A;
    private Entity RESULT_ENTITY_B;
    private Entity ACTUAL_ENTITY_A;
    private PreparedQuery preparedQuery;

    @Before
    public void setUp() {
        // NOTE: Instantiating entities before performing helper.setUp() causes API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();
        SaveMapImageDatastore = new SaveMapImageDatastore();

        MAPIMAGE_A = new MapImage(40.7128, -74.0060, "New York, NY", 5, 7, 2020, 1596054321);
        MAPIMAGE_A.setObjectID();
        RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A, "ResultEntity");
        datastore.put(RESULT_ENTITY_A);

        MAPIMAGE_B = new MapImage(35.9128, -100.3821, "Canadian, TX", 5, 7, 2020, 1596051621);
        MAPIMAGE_B.setObjectID();
        RESULT_ENTITY_B = SaveMapImageDatastore.createEntity(MAPIMAGE_B, "ResultEntity");
 
        // Set up Datastore entities.
        ACTUAL_ENTITY_A = new Entity("MapImageEntity");
        ACTUAL_ENTITY_A.setProperty("Latitude", 40.7128);
        ACTUAL_ENTITY_A.setProperty("Longitude", -74.0060);
        ACTUAL_ENTITY_A.setProperty("City Name", "New York, NY");
        ACTUAL_ENTITY_A.setProperty("Zoom", 5);
        ACTUAL_ENTITY_A.setProperty("Month", 7);
        ACTUAL_ENTITY_A.setProperty("Year", 2020);
        ACTUAL_ENTITY_A.setProperty("Timestamp", 1596054321);

        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        preparedQuery = datastore.prepare(query);
    }

    @After
    public void done() {
        helper.tearDown();
    }
    
    // First 7 tests cover assignment of setting entity properties
    @Test
    public void testLatCreateEntity() {
        double latitude = (double) RESULT_ENTITY_A.getProperty("Latitude");
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), latitude, 1e-15); 
    }

    @Test
    public void testLongiCreateEntity() {
        double longitude = (double) RESULT_ENTITY_A.getProperty("Longitude");
        Assert.assertEquals(MAPIMAGE_A.getLongitude(), longitude, 1e-15);
    }

    @Test
    public void testLocCreateEntity() {
        String cityName = (String) RESULT_ENTITY_A.getProperty("City Name");
        Assert.assertEquals(MAPIMAGE_A.getCityName(), cityName);
    }

    @Test
    public void testZoomCreateEntity() {
        int zoom = (Integer) (RESULT_ENTITY_A.getProperty("Zoom"));
        Assert.assertEquals(MAPIMAGE_A.getZoom(), zoom);
    }

    @Test
    public void testMonthCreateEntity() {
        int month = (Integer) (RESULT_ENTITY_A.getProperty("Month"));
        Assert.assertEquals(MAPIMAGE_A.getMonth(), month);
    }

    @Test
    public void testYearCreateEntity() {
        int year = (Integer) (RESULT_ENTITY_A.getProperty("Year"));
        Assert.assertEquals(MAPIMAGE_A.getYear(), year);
    }

    @Test
    public void testTimeCreateEntity() {
        long timeStamp = (long) RESULT_ENTITY_A.getProperty("Timestamp");
        Assert.assertEquals(MAPIMAGE_A.getTimeStamp(), timeStamp);
    }

    @Test
    public void checkQuerySizeIsOne() {
        Assert.assertEquals(1, preparedQuery.countEntities());
    }

    // Next 7 tests check proper metadata storage
    @Test
    public void checkStoredPropertyLat() {
        double latitude = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            latitude = (double) RESULT_ENTITY.getProperty("Latitude");
        }
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), latitude, 1e-15);
    }

    @Test
    public void checkStoredPropertyLongi() {
        double longitude = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            longitude = (double) RESULT_ENTITY.getProperty("Longitude");
        }
        Assert.assertEquals(MAPIMAGE_A.getLongitude(), longitude, 1e-15);
    }

    @Test
    public void checkStoredPropertyLoc() {
        String cityName = "";
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            cityName = (String) RESULT_ENTITY.getProperty("City Name");
        }
        Assert.assertEquals(MAPIMAGE_A.getCityName(), cityName);
    }

    @Test
    public void checkStoredPropertyZoom() {
        int zoom = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            zoom = toIntExact((long) (RESULT_ENTITY.getProperty("Zoom")));
        }
        Assert.assertEquals(MAPIMAGE_A.getZoom(), zoom);
    }

    @Test
    public void checkStoredPropertyMonth() {
        int month = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            month = toIntExact((long) (RESULT_ENTITY.getProperty("Month")));
        }
        Assert.assertEquals(MAPIMAGE_A.getMonth(), month);
    }

    @Test
    public void checkStoredPropertyYear() {
        int year = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            year = toIntExact((long) (RESULT_ENTITY.getProperty("Year")));
        }
        Assert.assertEquals(MAPIMAGE_A.getYear(), year);
    }

    @Test
    public void checkStoredPropertyTime() {
        long timeStamp = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            timeStamp = (long) RESULT_ENTITY.getProperty("Timestamp");
        }
        Assert.assertEquals(MAPIMAGE_A.getTimeStamp(), timeStamp);
    }

    @Test
    public void testMultipleEntitiesSize() {
        datastore.put(RESULT_ENTITY_B);
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery multipleEntities = datastore.prepare(query);
        Assert.assertEquals(2, multipleEntities.countEntities());
    }

    @Test
    public void testSort() {
        datastore.put(RESULT_ENTITY_B);
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery multipleEntities = datastore.prepare(query);
        List<String> sortedLocations = new ArrayList<String>();
        for (Entity RESULT_ENTITY : multipleEntities.asIterable()) {
            sortedLocations.add((String) RESULT_ENTITY.getProperty("City Name"));
        }
        Assert.assertEquals(Arrays.asList("Canadian, TX", "New York, NY"), sortedLocations);
    }
}
