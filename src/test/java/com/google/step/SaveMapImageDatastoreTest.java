package com.google.step;
 
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;
import static java.lang.Math.toIntExact;
 
import java.util.List;
import java.util.ArrayList;
 
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
public final class SaveMapImageDatastoreTest {
    private DatastoreService datastore;
    private SaveMapImageDatastore SaveMapImageDatastore;
 
    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
    private MapImage MAPIMAGE_A;
    private Entity ACTUAL_ENTITY_A;
 
    @Before
    public void setUp() {
        //NOTE: Instantiating Entities before performing helper.setUp() causes API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();
        SaveMapImageDatastore = new SaveMapImageDatastore();
        
        MAPIMAGE_A = new MapImage(35.9128, -100.3821, "Canadian, TX", 5, 7, 2020, 1596051621);
 
        // Set up Datastore entities.
        ACTUAL_ENTITY_A = new Entity("MapImageEntity");
        ACTUAL_ENTITY_A.setProperty("Latitude", 35.9128);
        ACTUAL_ENTITY_A.setProperty("Longitude", -100.3821);
        ACTUAL_ENTITY_A.setProperty("City Name", "Canadian, TX");
        ACTUAL_ENTITY_A.setProperty("Zoom", 5);
        ACTUAL_ENTITY_A.setProperty("Month", 7);
        ACTUAL_ENTITY_A.setProperty("Year", 2020);
        ACTUAL_ENTITY_A.setProperty("Timestamp", 1596051621);
    }
 
    @After
    public void done() {
        helper.tearDown();
    }
    
    //First 7 tests cover assignment of setting entity properties
    @Test
    public void testLatCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        double latitude = (double) RESULT_ENTITY_A.getProperty("Latitude");
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), latitude);
        
    }

    @Test
    public void testLongiCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        double longitude = (double) RESULT_ENTITY_A.getProperty("Longitude");
        Assert.assertEquals(MAPIMAGE_A.getLongitude(), longitude);
    }   

    @Test
    public void testZoomCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        long zoom = (long) RESULT_ENTITY_A.getProperty("Zoom");
        Assert.assertEquals(MAPIMAGE_A.getZoom(), zoom);
    }

    @Test
    public void testLocCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        String cityName = (String) RESULT_ENTITY_A.getProperty("City Name");
        Assert.assertEquals(MAPIMAGE_A.getCityName(), cityName));
    }

    @Test
    public void testMonthCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        long month = (long) RESULT_ENTITY_A.getProperty("Month");
        Assert.assertEquals(MAPIMAGE_A.getMonth(), month);
    }

    @Test
    public void testYearCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        long year = (long) RESULT_ENTITY_A.getProperty("Year");
        Assert.assertEquals(MAPIMAGE_A.getYear(), year);
    }

    @Test
    public void testTimeCreateEntity() {
        Entity RESULT_ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        long timeStamp = (long) RESULT_ENTITY_A.getProperty("Timestamp");
        Assert.assertEquals(MAPIMAGE_A.getTimeStamp(), timeStamp);
    }

    @Test
    public void checkQuerySizeIsOne() {
        // Put entity into datastore.
        Entity ENTITY_A = SaveMapImageDatastore.createEntity(MAPIMAGE_A,"ResultEntity");
        datastore.put(ENTITY_A);

        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        Assert.assertEquals(1, result.countEntities());
    }

    @Test
    public void checkStoredPropertyLat() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        double latitude = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            latitude = (double) RESULT_ENTITY.getProperty("Latitude");
        }
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), latitude, 1e-15);
    }

    @Test
    public void checkStoredPropertyLongi() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        double longitude = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            longitude = (double) RESULT_ENTITY.getProperty("Longitude");
        }
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), longitude, 1e-15);
    }

    @Test
    public void checkStoredPropertyLoc() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        String cityName = "";
        for (Entity RESULT_ENTITY : result.asIterable()) {
            cityName = (String) RESULT_ENTITY.getProperty("City Name");
        }
        Assert.assertEquals(MAPIMAGE_A.getCityName(), cityName);
    }

    @Test
    public void checkStoredPropertyZoom() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        int zoom = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            zoom = toIntExact((long) RESULT_ENTITY.getProperty("Zoom"));
        }
        Assert.assertEquals(MAPIMAGE_A.getZoom(), zoom);
    }

    @Test
    public void checkStoredPropertyMonth() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        int month = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            month = toIntExact((long) RESULT_ENTITY.getProperty("Month"));
        }
        Assert.assertEquals(MAPIMAGE_A.getMonth(), month);
    }

    @Test
    public void checkStoredPropertyYear() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        int year = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            year = toIntExact((long) RESULT_ENTITY.getProperty("Year"));
        }
        Assert.assertEquals(MAPIMAGE_A.getYear(), year);
    }

    @Test
    public void checkStoredPropertyTime() {
        // Load entity from Datastore.
        Query query = new Query("ResultEntity").addSort("City Name", SortDirection.ASCENDING);
        PreparedQuery result = datastore.prepare(query);
        long timeStamp = 0;
        for (Entity RESULT_ENTITY : result.asIterable()) {
            timeStamp = (long) RESULT_ENTITY.getProperty("Timestamp");
        }
        Assert.assertEquals(MAPIMAGE_A.getTimeStamp(), timeStamp);
    }           
}
