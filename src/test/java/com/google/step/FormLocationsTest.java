package com.google.step;

import static java.lang.Math.toIntExact;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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
public final class FormLocationsTest {
    private DatastoreService datastore;
    private FormLocations formLocations;
    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private Entity TEST_ENTITY_A;
    private Entity TEST_ENTITY_B;
    private Entity ACTUAL_ENTITY_A;
    private Entity ACTUAL_ENTITY_B;
    private MapImage MAPIMAGE_A;
    private MapImage MAPIMAGE_B;

    @Before
    public void setUp() {
        // NOTE: Instantiating entities before performing helper.setUp() causes API error.
        helper.setUp();
        formLocations = new FormLocations();

        // Set up test Datastore entities.
        TEST_ENTITY_A = new Entity("TrackedLocation");
        TEST_ENTITY_A.setProperty("cityName", "New York, NY");
        TEST_ENTITY_A.setProperty("latitude", 40.7128);
        TEST_ENTITY_A.setProperty("longitude", -74.0060);

        TEST_ENTITY_B = new Entity("TrackedLocation");
        TEST_ENTITY_B.setProperty("cityName", "Canadian, TX");
        TEST_ENTITY_B.setProperty("latitude", 35.9128);
        TEST_ENTITY_B.setProperty("longitude", -100.3821);

        datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(TEST_ENTITY_A);
        datastore.put(TEST_ENTITY_B);

        // Set up actual Datastore entities.
        ACTUAL_ENTITY_A = new Entity("Actual Tracked Location");
        ACTUAL_ENTITY_A.setProperty("cityName", "New York, NY");
        ACTUAL_ENTITY_A.setProperty("latitude", 40.7128);
        ACTUAL_ENTITY_A.setProperty("longitude", -74.0060);

        ACTUAL_ENTITY_B = new Entity("Actual Tracked Location");
        ACTUAL_ENTITY_B.setProperty("cityName", "Canadian, TX");
        ACTUAL_ENTITY_B.setProperty("latitude", 35.9128);
        ACTUAL_ENTITY_B.setProperty("longitude", -100.3821);

        // Set up test MapImages
        MAPIMAGE_A = new MapImage(40.7128, -74.0060, "New York, NY");
        MAPIMAGE_B = new MapImage(35.9128, -100.3821, "Canadian, TX");
    }

    @After
    public void done() {
        helper.tearDown();
    }

    @Test
    public void checkQuerySizeIsTwo() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        Assert.assertEquals(2, preparedQuery.countEntities());
    }

    @Test
    public void testSort() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<String> sortedLocations = new ArrayList<String>();
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            sortedLocations.add((String) RESULT_ENTITY.getProperty("cityName"));
        }
        Assert.assertEquals(Arrays.asList("Canadian, TX", "New York, NY"), sortedLocations);
    }

    // Next 6 tests check proper metadata retrieval
    @Test
    public void testRetrieveALat() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        double[] latitudes = new double[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            latitudes[i] = (double) RESULT_ENTITY.getProperty("latitude");
            i++;
        }
        Assert.assertEquals((double) ACTUAL_ENTITY_A.getProperty("latitude"), latitudes[1], 1e-15);
    }

    @Test
    public void testRetrieveBLat() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        double[] latitudes = new double[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            latitudes[i] = (double) RESULT_ENTITY.getProperty("latitude");
            i++;
        }
        Assert.assertEquals((double) ACTUAL_ENTITY_B.getProperty("latitude"), latitudes[0], 1e-15);
    }

    @Test
    public void testRetrieveALongi() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        double[] longitudes = new double[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            longitudes[i] = (double) RESULT_ENTITY.getProperty("longitude");
            i++;
        }
        Assert.assertEquals(
                (double) ACTUAL_ENTITY_A.getProperty("longitude"), longitudes[1], 1e-15);
    }

    @Test
    public void testRetrieveBLongi() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        double[] longitudes = new double[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            longitudes[i] = (double) RESULT_ENTITY.getProperty("longitude");
            i++;
        }
        Assert.assertEquals(
                (double) ACTUAL_ENTITY_B.getProperty("longitude"), longitudes[0], 1e-15);
    }

    @Test
    public void testRetrieveALoc() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        String[] locations = new String[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            locations[i] = (String) RESULT_ENTITY.getProperty("cityName");
            i++;
        }
        Assert.assertEquals((String) ACTUAL_ENTITY_A.getProperty("cityName"), locations[1]);
    }

    @Test
    public void testRetrieveBLoc() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        String[] locations = new String[2];
        int i = 0;
        for (Entity RESULT_ENTITY : preparedQuery.asIterable()) {
            locations[i] = (String) RESULT_ENTITY.getProperty("cityName");
            i++;
        }
        Assert.assertEquals((String) ACTUAL_ENTITY_B.getProperty("cityName"), locations[0]);
    }

    // Next 6 tests check proper MapImage List metadata
    @Test
    public void testMapImageListLocA() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_A.getCityName(), (resultList.get(1)).getCityName());
    }

    @Test
    public void testMapImageListLatA() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_A.getLatitude(), resultList.get(1).getLatitude(), 1e-15);
    }

    @Test
    public void testMapImageListLongiA() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_A.getLongitude(), resultList.get(1).getLongitude(), 1e-15);
    }

    @Test
    public void testMapImageListLocB() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_B.getCityName(), resultList.get(0).getCityName());
    }

    @Test
    public void testMapImageListLatB() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_B.getLatitude(), resultList.get(0).getLatitude(), 1e-15);
    }

    @Test
    public void testMapImageListLongiB() {
        PreparedQuery preparedQuery = formLocations.getQuery();
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(MAPIMAGE_B.getLongitude(), resultList.get(0).getLongitude(), 1e-15);
    }

    @Test
    public void testEmptyPreparedQuery() {
        Query query =
                new Query("No Tracked Loc Entities").addSort("cityName", SortDirection.ASCENDING);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<MapImage> resultList = formLocations.convertMetdataToMapImages(preparedQuery);
        Assert.assertEquals(new ArrayList<MapImage>(), resultList);
    }
}
