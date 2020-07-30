package com.google.step;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private LocalDateTime time;

    @Before
    public void setUp() {
        saveCloud = new SaveImageCloud();
        time = LocalDateTime.of(2020, 7, 10, 9, 30);
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
    public void notNullImageData() throws IOException {
        String url =
                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984";
        byte[] imageData = saveCloud.getImageData(url);
        Assert.assertNotNull(imageData);
    }

    @Test
    public void saveImageGetBlobNotNull() {
        // TODO: Figure out how to mock GCS
        // Blob expected = saveCloud.saveImageToCloudStorage();
        assert true;
    }
}
