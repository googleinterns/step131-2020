package com.google.step;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SaveDriveTest {
    private SaveDrive saveDrive;
    private Storage storage;
    private LocalStorageHelper storageHelper;
    private Drive drive;
    private Drive.Files files;
    private Drive.Files.Create filesCreate;
    private Drive.Files.List filesList;

    private final LocalDateTime TIME = LocalDateTime.of(2020, 7, 10, 9, 30);
    private final MapImage MAP_IMAGE_1 =
            new MapImage("Test Location", 47.345, 34.456).updateMetadata(TIME);

    private final byte[] GCS_CONTENT = "test".getBytes();
    private final String PARENT_ID_1 = "parent1";
    private final String PARENT_ID_2 = "parent2";
    private final String PARENT_ID_3 = "parent3";
    private final String FILE_ID = "file1";
    private final File EXPECTED_FILE = new File();
    private final FileList EXPECTED_FILE_LIST = new FileList();
    private final List<File> TEST_FILES = new ArrayList<>();

    @Before
    public void setUp() throws IOException {
        saveDrive = new SaveDrive();
        storage = LocalStorageHelper.getOptions().getService();
        drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        files = mock(Drive.Files.class);
        filesCreate = mock(Drive.Files.Create.class);
        filesList = mock(Drive.Files.List.class);
        EXPECTED_FILE.setName("test.txt");
        EXPECTED_FILE.setId(FILE_ID);
        EXPECTED_FILE_LIST.setFiles(new ArrayList<File>());
        when(drive.files()).thenReturn(files);
        when(drive.files().create(any(File.class))).thenReturn(filesCreate);
        when(drive.files().create(any(File.class)).setSupportsAllDrives(true))
                .thenReturn(filesCreate);
        when(drive.files().create(any(File.class)).setFields(anyString())).thenReturn(filesCreate);
        when(drive.files().create(any(File.class)).execute()).thenReturn(EXPECTED_FILE);
        when(drive.files().create(any(File.class), any(InputStreamContent.class)))
                .thenReturn(filesCreate);
        when(drive.files()
                        .create(any(File.class), any(InputStreamContent.class))
                        .setSupportsAllDrives(true))
                .thenReturn(filesCreate);
        when(drive.files()
                        .create(any(File.class), any(InputStreamContent.class))
                        .setFields(anyString()))
                .thenReturn(filesCreate);
        when(drive.files().create(any(File.class), any(InputStreamContent.class)).execute())
                .thenReturn(EXPECTED_FILE);
        when(drive.files().list()).thenReturn(filesList);
        when(drive.files().list().setQ(anyString())).thenReturn(filesList);
        when(drive.files().list().setDriveId(anyString())).thenReturn(filesList);
        when(drive.files().list().setSpaces(anyString())).thenReturn(filesList);
        when(drive.files().list().setIncludeItemsFromAllDrives(true)).thenReturn(filesList);
        when(drive.files().list().setCorpora(anyString())).thenReturn(filesList);
        when(drive.files().list().setFields(anyString())).thenReturn(filesList);
        when(drive.files().list().setSupportsAllDrives(true)).thenReturn(filesList);
        when(drive.files().list().execute()).thenReturn(EXPECTED_FILE_LIST);
    }

    @Test
    public void testUploadFile() throws IOException {
        String data = "testing";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        File actual = saveDrive.uploadFile(drive, EXPECTED_FILE, inputStream);
        assertEquals(EXPECTED_FILE, actual);
    }

    @Test
    public void testCreateFolder() throws IOException {
        File actual = saveDrive.createFolder(drive, PARENT_ID_1, PARENT_ID_2);
        assertEquals(EXPECTED_FILE, actual);
    }

    @Test
    public void testGetDriveItem() throws IOException {
        FileList actual = saveDrive.getDriveItem(drive, PARENT_ID_1, PARENT_ID_3, "text/plain");
        assertEquals(EXPECTED_FILE_LIST, actual);
    }

    @Test
    public void testGetParentFolderID() throws IOException {
        String actual = saveDrive.getParentFolderID(drive, MAP_IMAGE_1);
        assertEquals(FILE_ID, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetParentFolderIDNullImage() throws IOException {
        String actual = saveDrive.getParentFolderID(drive, null);
    }
}
