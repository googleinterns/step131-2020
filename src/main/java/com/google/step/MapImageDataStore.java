 package com.google.step;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.cloud.storage.Blob.BlobSourceOption;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

// TODO: Tests

@WebServlet("/save-datastore")
public class MapImageDataStore extends HttpServlet {

    /** Stores MapImage object into Datastore. */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // TODO: Send MapImage objects here. OR combine this function into SaveImages.java servlet
        MapImage mapImage = null;

        // Creat new entity of kind MapImage and look-up key is MapImage's object name.
        Entity mapImageEntity = new Entity("MapImage", mapImage.getObjectID());

        mapImageEntity.setProperty("Longitude", mapImage.getLongitude());
        mapImageEntity.setProperty("Latitude", mapImage.getLatitude());
        mapImageEntity.setProperty("City Name", mapImage.getCityName());
        mapImageEntity.setProperty("Zoom", mapImage.getZoom());
        mapImageEntity.setProperty("Month", mapImage.getMonth());
        mapImageEntity.setProperty("Year", mapImage.getYear());
        mapImageEntity.setProperty("Time Stamp", mapImage.getTimeStamp());

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(mapImageEntity);

        // TODO: adjust redirect location
        response.sendRedirect("/index.html");
    }

}