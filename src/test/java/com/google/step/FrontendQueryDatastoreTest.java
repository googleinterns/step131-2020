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
    private final long JULY_9_2020_EPOCH = JULY_9_2020.toEpochSecond(ZoneOffset.UTC);
    private final String JULY_9_2020_STRING = String.valueOf(JULY_9_2020.toEpochSecond(ZoneOffset.UTC));
    private final LocalDateTime JULY_31_2020 = LocalDateTime.of(2020, 7, 31, 12, 30);
    private final long JULY_31_2020_EPOCH = JULY_31_2020.toEpochSecond(ZoneOffset.UTC);
    private final String JULY_31_2020_STRING = String.valueOf(JULY_31_2020.toEpochSecond(ZoneOffset.UTC));
    private final LocalDateTime AUGUST_5_2020 = LocalDateTime.of(2020, 8, 5, 18, 30);
    private final long AUGUST_5_2020_EPOCH = AUGUST_5_2020.toEpochSecond(ZoneOffset.UTC);
    private final String AUGUST_5_2020_STRING = String.valueOf(AUGUST_5_2020.toEpochSecond(ZoneOffset.UTC));


    @Before
    public void setUp() {
        //NOTE: Instantiating Entities before performing helper.setUp() causes the API error.
        helper.setUp();
        datastore = DatastoreServiceFactory.getDatastoreService();

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
        Filter actual = frontendQueryDatastore.buildDateFilters(
            JULY_9_2020_EPOCH, JULY_31_2020_EPOCH);

        Filter expected = new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_9_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", JULY_31_2020_EPOCH)
        ));
        
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void buildZoomFilters_NoInput() {
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

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void buildCityFilters_NoInput() {
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

    @Test
    public void buildCityFilters_OneInput() {
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");

        Filter actual = frontendQueryDatastore.buildCityFilters(cityStrings);
        Filter expected = FilterOperator.EQUAL.of("City Name", "London");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter() {
        // Set up each value from the "form".
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("6");
        zoomStrings.add("12");
        zoomStrings.add("15");
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            zoomStrings, cityStrings, JULY_9_2020_STRING, JULY_31_2020_STRING);

        // Set up each expected filter (city, zoom, date) individually.
        ArrayList<Filter> expected_filters = new ArrayList<>();
        // Add city filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        )));
        // Add zoom filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 6),
                FilterOperator.EQUAL.of("Zoom", 12),
                FilterOperator.EQUAL.of("Zoom", 15)
        )));
        // Add date filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_9_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", JULY_31_2020_EPOCH)
        )));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_OnlyZooms() {
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("5");
        zoomStrings.add("13");
        zoomStrings.add("18");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            zoomStrings, EMPTY_STRING_ARRAY, "", "");

        ArrayList<Filter> expected_filters = new ArrayList<>();
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 5),
                FilterOperator.EQUAL.of("Zoom", 13),
                FilterOperator.EQUAL.of("Zoom", 18)
        )));
        // We duplicate the filter to bypass Datastore's requirement
        // for a composite filter to have two subfilters.
        expected_filters.add(expected_filters.get(0));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_OnlyCities() {
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            EMPTY_STRING_ARRAY, cityStrings, "", "");

        ArrayList<Filter> expected_filters = new ArrayList<>();
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        )));
        // We duplicate the filter to bypass Datastore's requirement
        // for a composite filter to have two subfilters.
        expected_filters.add(expected_filters.get(0));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_OnlyDates() {
        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, JULY_9_2020_STRING, JULY_31_2020_STRING);

        ArrayList<Filter> expected_filters = new ArrayList<>();
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_9_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", JULY_31_2020_EPOCH)
        )));
        // We duplicate the filter to bypass Datastore's requirement
        // for a composite filter to have two subfilters.
        expected_filters.add(expected_filters.get(0));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_CityAndZoom() {
        // Set up each value from the "form".
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("6");
        zoomStrings.add("12");
        zoomStrings.add("15");
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            zoomStrings, cityStrings, "", "");

        // Set up each expected filter (city, zoom) individually.
        ArrayList<Filter> expected_filters = new ArrayList<>();
        // Add city filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        )));
        // Add zoom filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 6),
                FilterOperator.EQUAL.of("Zoom", 12),
                FilterOperator.EQUAL.of("Zoom", 15)
        )));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_CityAndDate() {
        // Set up each value from the "form".
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            EMPTY_STRING_ARRAY, cityStrings, JULY_9_2020_STRING, AUGUST_5_2020_STRING);

        // Set up each expected filter (city, date) individually.
        ArrayList<Filter> expected_filters = new ArrayList<>();
        // Add city filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        )));
        // Add date filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_9_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", AUGUST_5_2020_EPOCH)
        )));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildCompositeFilter_ZoomAndDate() {
        // Set up each value from the "form".
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("6");
        zoomStrings.add("12");
        zoomStrings.add("15");

        Filter actual = frontendQueryDatastore.buildCompositeFilter(
            zoomStrings, EMPTY_STRING_ARRAY, JULY_31_2020_STRING, AUGUST_5_2020_STRING);

        // Set up each expected filter (zoom, date) individually.
        ArrayList<Filter> expected_filters = new ArrayList<>();
        // Add zoom filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 6),
                FilterOperator.EQUAL.of("Zoom", 12),
                FilterOperator.EQUAL.of("Zoom", 15)
        )));
        // Add date filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_31_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", AUGUST_5_2020_EPOCH)
        )));
        Filter expected = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void buildQuery() {
        // Set up each value from the "form".
        ArrayList<String> zoomStrings = new ArrayList<>();
        zoomStrings.add("6");
        zoomStrings.add("12");
        zoomStrings.add("15");
        ArrayList<String> cityStrings = new ArrayList<>();
        cityStrings.add("London");
        cityStrings.add("New York City");
        cityStrings.add("Tokyo");

        CompositeFilter actualCompositeFilter = frontendQueryDatastore.buildCompositeFilter(
            zoomStrings, cityStrings, JULY_9_2020_STRING, JULY_31_2020_STRING);
        Query actual = frontendQueryDatastore.buildQuery(actualCompositeFilter);

        // Set up each expected filter (city, zoom, date) individually.
        ArrayList<Filter> expected_filters = new ArrayList<>();
        // Add city filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("City Name", "London"),
                FilterOperator.EQUAL.of("City Name", "New York City"),
                FilterOperator.EQUAL.of("City Name", "Tokyo")
        )));
        // Add zoom filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.OR, Arrays.asList(
                FilterOperator.EQUAL.of("Zoom", 6),
                FilterOperator.EQUAL.of("Zoom", 12),
                FilterOperator.EQUAL.of("Zoom", 15)
        )));
        // Add date filters.
        expected_filters.add(new CompositeFilter(
            CompositeFilterOperator.AND, Arrays.asList(
                FilterOperator.GREATER_THAN_OR_EQUAL.of("Timestamp", JULY_9_2020_EPOCH),
                FilterOperator.LESS_THAN_OR_EQUAL.of("Timestamp", JULY_31_2020_EPOCH)
        )));

        Filter expectedCompositeFilter = new CompositeFilter(CompositeFilterOperator.AND, expected_filters);
        Query expected = new Query("MapImage")
            .setFilter(expectedCompositeFilter)
            .addSort("Timestamp", SortDirection.ASCENDING)
            .addSort("Zoom", SortDirection.ASCENDING)
            .addSort("City Name", SortDirection.ASCENDING);

        Assert.assertEquals(expected, actual);
    }
}
