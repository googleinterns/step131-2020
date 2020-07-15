package com.google.step;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.util.ArrayList;
import java.util.List;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob.BlobSourceOption;
import javax.imageio.ImageIO;
import java.net.URL;
import com.google.api.client.http.InputStreamContent;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Query;
import java.util.logging.Logger;
import com.google.apphosting.api.ApiProxy.CancelledException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.apphosting.api.DeadlineExceededException;
import java.io.PrintWriter;

/***
    This servlet will be used to retrieve images
    from the shared Google Drive
***/
@WebServlet(
    name = "SaveDrive",
    description = "taskqueue: Save images to Drive",
    urlPatterns = "/save-drive"
)
public class SaveDrive extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String BUCKET_NAME = String.format("%s.appspot.com", PROJECT_ID);
    // The unique identifier for the shared Google Drive
    private final String DRIVE_ID = "0AJnQ8N4V8NrAUk9PVA";
    private final static Logger LOGGER = Logger.getLogger(SaveDrive.class.getName());
    private Drive drive = null;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getParameter("accessToken");
        if(!accessToken.equals("")) {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            drive = new Drive.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
                .setApplicationName(PROJECT_ID)
                .build();
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
            Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("DriveMapImage")
                .build();
            // Get objects from Datastore
            QueryResults<Entity> resultList = datastore.run(query);
            ArrayList<MapImage> mapImages = entitiesToMapImages(resultList);
            for(MapImage image : mapImages) {
                try {
                    // Get url from Storage
                    URL url = getFileURL(storage, image);
                    // Generate file metadata
                    File fileMetadata = new File();
                    fileMetadata.setName(image.getObjectID().replaceAll("/", "_"));
                    // Get the parent folder for the image
                    String parentFolderID = getParentFolderID(image);
                    fileMetadata.setParents(Collections.singletonList(parentFolderID));
                    // Upload file
                    InputStreamContent isc = new InputStreamContent("image/png", url.openStream());
                    File file = drive.files().create(fileMetadata, isc)
                        .set("supportsAllDrives", true)
                        .setFields("id")
                        .execute();
                    // Delete the DriveMapImage entity from Datastore
                    Key key = Key.newBuilder(PROJECT_ID, "DriveMapImage", image.getObjectID()).build();
                    datastore.delete(key);
                }
                catch(CancelledException | GoogleJsonResponseException | DeadlineExceededException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    private ArrayList<MapImage> entitiesToMapImages(QueryResults<Entity> resultList) {
        ArrayList<MapImage> resultMapImages = new ArrayList<>();
        while (resultList.hasNext()) {  // While we still have data
            resultMapImages.add(entityToMapImage(resultList.next())); // Add the MapImage to the List
        }
        return resultMapImages;
    }

    private MapImage entityToMapImage(Entity entity) {
        /* 
        *   NOTE: entity.get"Type" (i.e. entity.getDouble) will return either DatastoreException
        *   if the property doesn't exist, or a ClassCastException if the value is the wrong type
        */

        double latitude = entity.getDouble("Latitude");
        double longitude = entity.getDouble("Longitude");
        int zoom = (int) entity.getLong("Zoom");
        String cityName = entity.getString("City Name");
        int month = (int) entity.getLong("Month");
        int year = (int) entity.getLong("Year");
        String timeStamp = entity.getString("Time Stamp");

        MapImage mapImage = new MapImage(longitude, latitude, cityName, zoom, month, year, timeStamp);
        mapImage.setObjectID();
        return mapImage;
    }
    
    private URL getFileURL(Storage storage, MapImage mapImage) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, mapImage.getObjectID()).build();
        return storage.signUrl(blobInfo, 3, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
    }

    private String getParentFolderID(MapImage mapImage) throws IOException {
        String yearString = Integer.toString(mapImage.getYear());
        String yearFolderID = "";
        String monthString = Integer.toString(mapImage.getMonth());
        String monthFolderID = "";
        String cityString = mapImage.getCityName();
        String cityFolderID = "";
        // Create year folder if it does not exist
        FileList result = drive.files().list()
            .setQ(String.format("mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s'", yearString))
            .setSpaces("drive")
            .setDriveId(DRIVE_ID)
            .setIncludeItemsFromAllDrives(true) // Required parameter for querying shared drives
            .setCorpora("drive")                // Required parameter for querying shared drives
            .setSupportsAllDrives(true)         // Required parameter for querying shared drives
            .setFields("files(id, name)")
            .execute();
            // Check if the folder exists
        if(result.getFiles().size() == 0) {
            File fileMetadata = new File();
            fileMetadata.setName(yearString);
            fileMetadata.setParents(Collections.singletonList(DRIVE_ID));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = drive.files().create(fileMetadata)
                .setFields("id")
                .setSupportsAllDrives(true)
                .execute();
            yearFolderID = file.getId();
        }
        else {
            File file = result.getFiles().get(0);
            yearFolderID = file.getId();
        }
        // Create month folder if it does not exist
        result = drive.files().list()
            .setQ(String.format("mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and parents in '%s'", monthString, yearFolderID))
            .setSpaces("drive")
            .setDriveId(DRIVE_ID)
            .setIncludeItemsFromAllDrives(true) // Required parameter for querying shared drives
            .setCorpora("drive")                // Required parameter for querying shared drives
            .setSupportsAllDrives(true)         // Required parameter for querying shared drives
            .setFields("files(id, name)")
            .execute();
        // Check if the folder exists
        if(result.getFiles().size() == 0) {
            File fileMetadata = new File();
            fileMetadata.setName(monthString);
            fileMetadata.setParents(Collections.singletonList(yearFolderID));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = drive.files().create(fileMetadata)
                .setFields("id")
                .setSupportsAllDrives(true)
                .execute();
            monthFolderID = file.getId();
        }
        else {
            File file = result.getFiles().get(0);
            monthFolderID = file.getId();
        }
        // Create city folder if it does not exist
        result = drive.files().list()
            .setQ(String.format("mimeType='application/vnd.google-apps.folder' and trashed=false and name='%s' and parents in '%s'", cityString, monthFolderID))
            .setSpaces("drive")
            .setDriveId(DRIVE_ID)
            .setIncludeItemsFromAllDrives(true) // Required parameter for querying shared drives
            .setCorpora("drive")                // Required parameter for querying shared drives
            .setSupportsAllDrives(true)         // Required parameter for querying shared drives
            .setFields("files(id, name)")
            .execute();
        // Check if the folder exists
        if(result.getFiles().size() == 0) {
            File fileMetadata = new File();
            fileMetadata.setName(cityString);
            fileMetadata.setParents(Collections.singletonList(monthFolderID));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = drive.files().create(fileMetadata)
                .setFields("id")
                .setSupportsAllDrives(true)
                .execute();
            cityFolderID = file.getId();
        }
        else {
            File file = result.getFiles().get(0);
            cityFolderID = file.getId();
        }
        return cityFolderID;
    }
}
