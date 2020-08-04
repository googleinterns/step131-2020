package com.google.step;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.api.datastore.dev.LocalDatastoreService.AutoIdAllocationPolicy;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

@RunWith(JUnit4.class)
public final class FrontendQueryDatastoreTest {
    private DatastoreService datastore;
    private FrontendQueryDatastore frontendQueryDatastore;

    // Local Datastore for testing purposes.
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    
    private final ArrayList<String> EMPTY_STRING_ARRAY = new ArrayList<>();
    private final LocalDateTime JULY_9_2020 = LocalDateTime.of(2020, 7, 9, 6, 30);
    private final LocalDateTime JULY_31_2020 = LocalDateTime.of(2020, 7, 31, 12, 30);
    private final LocalDateTime AUGUST_5_2020 = LocalDateTime.of(2020, 8, 5, 18, 30);


    @Before
    public void setUp() {
        //NOTE: Instantiating Entities before performing helper.setUp() causes the API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();

        // Set up Datastore entities.


        frontendQueryDatastore = new FrontendQueryDatastore();
    }

    @After
    public void done() {
        helper.tearDown();
    }

    @Test
    public void buildIndividualCityFilter() {
        Filter actual = frontendQueryDatastore.buildIndividualCityFilter("Tokyo");
        Filter expected = FilterOperator.EQUAL.of("City Name", "Tokyo");
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void buildIndividualZoomFilter() {
        Filter actual = frontendQueryDatastore.buildIndividualZoomFilter(7);
        Filter expected = FilterOperator.EQUAL.of("Zoom", 7);
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void buildDateFilters() {
        long july_9_2020_epoch = JULY_9_2020.toEpochSecond(ZoneOffset.UTC);
        long july_31_2020_epoch = JULY_31_2020.toEpochSecond(ZoneOffset.UTC);

        Filter actual = frontendQueryDatastore.buildDateFilters(
            july_9_2020_epoch, july_31_2020_epoch);

        Filter expected = new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", july_9_2020_epoch),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", july_31_2020_epoch)
        ));
        
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void buildZoomFilters_EmptyArray() {
        Filter actual = frontendQueryDatastore.buildZoomFilters(EMPTY_STRING_ARRAY);
    }

    @Test
    public void buildZoomFilters() {
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("5");
        zoomStrings.add("7");
        zoomStrings.add("16");

        Filter actual = frontendQueryDatastore.buildZoomFilters(zoomStrings);
        Filter expected = new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 5),
                FilterOperator.EQUAL.of("Zoom", 7),
                FilterOperator.EQUAL.of("Zoom", 16)
        ));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildZoomFilters_OneLetterInputAdded() {
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("5");
        zoomStrings.add("b");
        zoomStrings.add("16");

        Filter actual = frontendQueryDatastore.buildZoomFilters(zoomStrings);
        Filter expected = new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 5),
                FilterOperator.EQUAL.of("Zoom", 16)
        ));

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void buildZoomFilters_AllLetterInputs() {
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("a");
        zoomStrings.add("b");
        zoomStrings.add("c");

        Filter actual = frontendQueryDatastore.buildZoomFilters(zoomStrings);
    }

    @Test
    public void buildZoomFilters_OneZoom() {
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("12");

        Filter actual = frontendQueryDatastore.buildZoomFilters(zoomStrings);
        Filter expected = FilterOperator.EQUAL.of("Zoom", 12);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void buildCityFilters_EmptyArray() {
        Filter actual = frontendQueryDatastore.buildCityFilters(EMPTY_STRING_ARRAY);
    }

    @Test
    public void buildCityFilters() {
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        Filter actual = frontendQueryDatastore.buildCityFilters(cityStrings);
        Filter expected = new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        ));

        Assert.assertEquals(expected, actual);
    }

    // Lots of tests for buildCompositeFilter with only one argument (diff permuations)
}
