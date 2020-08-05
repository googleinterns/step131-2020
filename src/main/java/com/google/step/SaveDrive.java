package com.google.step;

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
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/** This servlet will be used to upload images to the shared Google Drive */
@WebServlet(
        name = "SaveDrive",
        description = "taskqueue: Save images to Drive",
        urlPatterns = "/save-drive")
public class SaveDrive extends HttpServlet {
    // The unique identifier for the shared Google Drive
    private final String DRIVE_ID = "0AJnQ8N4V8NrAUk9PVA";
    private static final Logger LOGGER = Logger.getLogger(SaveDrive.class.getName());

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String accessToken = request.getParameter("accessToken");
        if (!accessToken.equals("")) {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            Drive drive =
                    new Drive.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
                            .setApplicationName(CommonUtils.PROJECT_ID)
                            .build();
            Storage storage = StorageOptions.getDefaultInstance().getService();
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("DriveMapImage");
            // Get objects from Datastore
            PreparedQuery resultList = datastore.prepare(query);
            ArrayList<MapImage> mapImages = CommonUtils.entitiesToMapImages(resultList);
            for (MapImage image : mapImages) {
                try {
                    long timeRemaining = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                    // Stop uploading images if the task has less than 20 seconds remaining
                    if (timeRemaining < 20000) break;
                    // Get url from Storage
                    URL url = CommonUtils.getCloudFileURL(storage, image.getObjectID(), 3);
                    // Generate file metadata
                    File fileMetadata = new File();
                    fileMetadata.setName(image.getObjectID().replaceAll("/", "_"));
                    // Get the parent folder for the image
                    String parentFolderID = getParentFolderID(drive, image);
                    fileMetadata.setParents(Collections.singletonList(parentFolderID));
                    // Upload file
                    uploadFile(drive, fileMetadata, url.openStream());
                    // Delete the DriveMapImage entity from Datastore
                    Key key = new KeyFactory.Builder("DriveMapImage", image.getObjectID()).getKey();
                    datastore.delete(key);
                } catch (CancelledException | DeadlineExceededException | IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    /** Upload a file to Drive */
    public File uploadFile(Drive drive, File fileMetadata, InputStream inputStream) throws IOException {
        InputStreamContent isc = new InputStreamContent("image/png", inputStream);
        File file =
                drive.files()
                        .create(fileMetadata, isc)
                        .set("supportsAllDrives", true)
                        .setFields("id")
                        .execute();
        return file;
    }

    /** Get the parent folder of a Drive folder */
    public FileList getParentFolder(Drive drive, String parentID, String folderName) throws IOException {
        FileList result =
                drive.files()
                        .list()
                        .setQ(
                                String.format(
                                        "mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and parents in '%s'",
                                        folderName, parentID))
                        .setSpaces("drive")
                        .setDriveId(DRIVE_ID)
                        .setIncludeItemsFromAllDrives(true) // Required parameter for shared drives
                        .setCorpora("drive") // Required parameter for shared drives
                        .setSupportsAllDrives(true) // Required parameter for shared drives
                        .setFields("files(id, name)")
                        .execute();
        return result;
    }

    /** Create a folder in Drive */
    public File createFolder(Drive drive, String parentID, String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setParents(Collections.singletonList(parentID));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file =
                drive.files()
                        .create(fileMetadata)
                        .setFields("id")
                        .setSupportsAllDrives(true)
                        .execute();
        return file;
    }

    /** Get the parent folder ID in Drive of a corresponding MapImage */
    public String getParentFolderID(Drive drive, MapImage mapImage) throws IOException {
        String yearString = Integer.toString(mapImage.getYear());
        String yearFolderID = "";
        String monthString = Integer.toString(mapImage.getMonth());
        String monthFolderID = "";
        String cityString = mapImage.getCityName();
        String cityFolderID = "";

        // Check if the year folder exists
        FileList result = getParentFolder(drive, DRIVE_ID, yearString);
        if (result.getFiles().size() == 0) {
            File file = createFolder(drive, DRIVE_ID, yearString);
            yearFolderID = file.getId();
        } else {
            File file = result.getFiles().get(0);
            yearFolderID = file.getId();
        }

        // Check if the month folder exists
        result = getParentFolder(drive, yearFolderID, monthString);
        if (result.getFiles().size() == 0) {
            File file = createFolder(drive, yearFolderID, monthString);
            monthFolderID = file.getId();
        } else {
            File file = result.getFiles().get(0);
            monthFolderID = file.getId();
        }

        // Check if the city folder exists
        result = getParentFolder(drive, monthFolderID, cityString);
        if (result.getFiles().size() == 0) {
            File file = createFolder(drive, monthFolderID, cityString);
            cityFolderID = file.getId();
        } else {
            File file = result.getFiles().get(0);
            cityFolderID = file.getId();
        }
        return cityFolderID;
    }
}
