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

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    }

    private String getRequestURL(double latitude, double longitude, int zoomLevel) {
        return String.format("%s&center=%f,%f&zoom=%d", baseURL, latitude, longitude, zoomLevel);
    }
}
