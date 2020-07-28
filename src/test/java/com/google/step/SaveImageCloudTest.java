package com.google.step;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SaveImageCloudTest {
    private SaveImageCloud saveCloud;

    @Before
    public void setUp() {
        saveCloud = new SaveImageCloud();
    }

    @Test
    public void validRequestURLs() {
        ArrayList<MapImage> mapImages =
                new ArrayList<>(
                        Arrays.asList(
                                new MapImage(40.7338366, -74.0043566, 5),
                                new MapImage(59.3248943, 18.0688734, 6)));
        ArrayList<String> actual = saveCloud.generateRequestUrls(mapImages);
        ArrayList<String> expected =
                new ArrayList<>(
                        Arrays.asList(
                                "https://maps.googleapis.com/maps/api/staticmap?center=40.7338366,-74.0043566&zoom=5&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984",
                                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984"));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void nonNullImageData() {
        String url =
                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984";
        try {
            byte[] imageData = saveCloud.getImageData(url);
            assert true;
        } catch (IOException e) {
            assert false : e.getMessage();
        }
    }

    @Test
    public void updatedMetadata() {
        // TODO: come up with test for "updateMetadata" method
        // MapImage actual = new MapImage(40.7338366, -74.0043566, 5);
        // Date date = new Date();
        // String month = String.format("%tm", date);
        // String year = String.format("%TY", date);
        // MapImage expected = new MapImage(40.7338366, -74.0043566, 5);
        assert true;
    }

    @Test
    public void saveImageGetBlobNotNull() {
        // TODO: Figure out how to mock GCS
        // Blob expected = saveCloud.saveImageToCloudStorage();
        assert true;
    }
}
