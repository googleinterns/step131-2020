package com.google.step;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob.BlobSourceOption;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.lang.StringBuilder;
import com.google.gson.Gson;
import java.util.Arrays;
import java.io.BufferedReader;
import com.google.gson.reflect.TypeToken;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import com.google.auth.ServiceAccountSigner.SigningException;

/***
    This servlet retrieves binary image data from Cloud for corresponding MapImage instance.
    A POST request gets image data to convert to a URL then sets the 'url' attribute of its MapImage instance.
    A GET request writes the POST-modified MapImage ArrayList to write on the servlet.
***/
@WebServlet("/query-cloud")
public class QueryCloud extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String BUCKET_NAME = String.format("%s.appspot.com", PROJECT_ID);
    private final static Logger LOGGER = Logger.getLogger(QueryCloud.class.getName());
    private ArrayList<MapImage> mapImages = null;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handles the request that is made before the form is submitted (on page load).
        if(mapImages == null) {
            response.getWriter().println("{}"); 
        }
        // Handles the request that is made after the form is submitted.
        else {
            Gson gson = new Gson();
            String data = gson.toJson(mapImages);
            response.setContentType("application/json");
            response.getWriter().println(data);
            mapImages = null;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BufferedReader reader = request.getReader();
            Gson gson = new Gson();
            mapImages = gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>(){}.getType());
            Storage storage = StorageOptions.getDefaultInstance().getService();
            mapImages.forEach(image -> {
                BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, image.getObjectID()).build();
                String url = storage.signUrl(blobInfo, 5, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature()).toString();
                image.setURL(url);
            });
        }
        catch(SigningException e) {
            LOGGER.severe(e.getCause().getMessage());
        }
    }
}