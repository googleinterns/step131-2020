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
}
