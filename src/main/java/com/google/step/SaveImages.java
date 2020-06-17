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


@WebServlet("/save-images-job")
public class SaveImages extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String baseURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x512&scale=2&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984";
    private Date date = new Date();
    private String month = String.format("%tm", date);
    private String year = String.format("%TY", date);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    }

    private byte[] getImageData(String requestURL) throws IOException {
        URL url = new URL(requestURL);
        BufferedImage image = ImageIO.read(url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        return bos.toByteArray();
    }

    private void saveImageToCloudStorage(byte[] imageData, String city, int zoomLevel) throws StorageException {
        // Add underscores to city names with spaces
        city = city.replaceAll(" ", "_");
        String bucketName = String.format("%s.appspot.com", PROJECT_ID);
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        String objectID = String.format("%s_%dx_%s_%s.png", city, zoomLevel, month, year);
        BlobId blobId = BlobId.of(bucketName, objectID);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
        Blob blob = storage.create(blobInfo, imageData);
    }
}
