package com.google.step;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.Date;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

@RunWith(JUnit4.class)
public final class SaveImageCloudTest {
    private SaveImageCloud saveCloud;
    private LocalDateTime time;

    @Before
    public void setUp() {
        saveCloud = new SaveImageCloud();
        time = LocalDateTime.of(2020, 13, 10, 9, 30);
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
    public void notNullImageData() {
        String url =
                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984";
        try {
            byte[] imageData = saveCloud.getImageData(url);
            Assert.assertNotNull(imageData);
        } catch (IOException e) {
            assert false : e.getMessage();
        }
    }

    @Test
    public void saveImageGetBlobNotNull() {
        // TODO: Figure out how to mock GCS
        // Blob expected = saveCloud.saveImageToCloudStorage();
        assert true;
    }
}
