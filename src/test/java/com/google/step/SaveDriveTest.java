package com.google.step;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.CancelledException;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class SaveDriveTest {
    private SaveDrive saveDrive;
    private Storage storage;
    private LocalStorageHelper storageHelper;
    Drive drive;

    private final LocalDateTime TIME = LocalDateTime.of(2020, 7, 10, 9, 30);
    private final MapImage MAP_IMAGE_1 =
            new MapImage("Test Location", 47.345, 34.456).updateMetadata(TIME);

    private final byte[] GCS_CONTENT = "test".getBytes();

    @Before
    public void setUp() {
        saveDrive = new SaveDrive();
        storage = LocalStorageHelper.getOptions().getService();
        drive = mock(Drive.class);
    }

    @Test
    public void testCreateFolderWithFile() throws IOException {
        File expected = new File();
        expected.setName("test.txt");
        String data = "testing";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        when(drive.files().create(any(File.class), any(InputStreamContent.class)).execute()).thenReturn(expected);
        File actual = saveDrive.uploadFile(drive, expected, inputStream);
        assertEquals(expected, actual);
    }

    @Test
    public void testUploadFile() {
        File testMetadata = new File();
        assert true;
    }

   
}
