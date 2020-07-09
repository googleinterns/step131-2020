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
import java.util.List;
import java.lang.StringBuilder;
import com.google.gson.Gson;
import java.util.Arrays;
import java.io.BufferedReader;
import com.google.gson.reflect.TypeToken;
import java.util.logging.Logger;

@WebServlet("/query-cloud")
public class QueryCloud extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String BUCKET_NAME = String.format("%s.appspot.com", PROJECT_ID);
    private final static Logger LOGGER = Logger.getLogger(QueryCloud.class.getName());
    private ArrayList<MapImageWithImageBytes> items = null;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // The request is made before the form is submitted (on page load)
        if(items == null) {
            response.getWriter().println("{}"); 
        }
        // The request is made after the form is submitted
        else {
            Gson gson = new Gson();
            String data = gson.toJson(items);
            response.setContentType("application/json");
            response.getWriter().println(data);
            items = null;
        }
    }



    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        ArrayList<MapImage> mapImages = gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>(){}.getType());
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        Bucket bucket = storage.get(BUCKET_NAME);
        items = new ArrayList<>();
        mapImages.forEach(image -> {
            items.add(new MapImageWithImageBytes(image, bucket.get(image.getObjectID()).getContent()));
        });
    }


}

// This class represents a MapImage and its corresponding image data
class MapImageWithImageBytes {
    private MapImage image;
    private byte[] bytes;

    public MapImageWithImageBytes(MapImage image, byte[] bytes) {
        this.image = image;
        this.bytes = bytes;
    }

    public MapImage getMapImage() { return image; }

    public byte[] getBytes() { return bytes; }
}
