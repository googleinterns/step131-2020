package com.google.step;

import static org.junit.Assert.*;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SaveImageCloudTest {
    private SaveImageCloud saveCloud;
    private Storage storage;
    private LocalStorageHelper storageHelper;

    private final LocalDateTime TIME = LocalDateTime.of(2020, 7, 10, 9, 30);
    private final MapImage MAP_IMAGE_1 =
            new MapImage("Test Location", 47.345, 34.456).updateMetadata(TIME);
    private final BlobInfo BLOB_INFO_1 =
            BlobInfo.newBuilder(CommonUtils.BUCKET_NAME, MAP_IMAGE_1.getObjectID())
                    .setContentType("image/png")
                    .build();
    private final byte[] GCS_CONTENT = "test".getBytes();

    @Before
    public void setUp() {
        saveCloud = new SaveImageCloud();
        storage = LocalStorageHelper.getOptions().getService();
    }

    @Test
    public void validRequestURLs() {
        ArrayList<MapImage> mapImages =
                new ArrayList<>(
                        Arrays.asList(
                                new MapImage(40.7338366, -74.0043566, "New York City", 5),
                                new MapImage(59.3248943, 18.0688734, "Stockholm", 6)));
        ArrayList<String> actual = saveCloud.generateRequestUrls(mapImages);
        ArrayList<String> expected =
                new ArrayList<>(
                        Arrays.asList(
                                "https://maps.googleapis.com/maps/api/staticmap?center=40.7338366,-74.0043566&zoom=5&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984",
                                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984"));
        assertEquals(expected, actual);
    }

    @Test
    public void notNullImageData() throws IOException {
        String url =
                "https://maps.googleapis.com/maps/api/staticmap?center=59.3248943,18.0688734&zoom=6&size=640x640&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984";
        byte[] imageData = saveCloud.getImageData(url);
        assertNotNull(imageData);
    }

    @Test
    public void saveImageToGCSNotNull() {
        Blob result = saveCloud.saveImageToCloudStorage(storage, GCS_CONTENT, MAP_IMAGE_1);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveImageToGCSIllegalArgumentException() {
        saveCloud.saveImageToCloudStorage(storage, null, MAP_IMAGE_1);
    }
}
