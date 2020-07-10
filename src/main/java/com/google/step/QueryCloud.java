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
import java.util.concurrent.TimeUnit;
import com.google.auth.ServiceAccountSigner.SigningException;

@WebServlet("/query-cloud")
public class QueryCloud extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String BUCKET_NAME = String.format("%s.appspot.com", PROJECT_ID);
    private final static Logger LOGGER = Logger.getLogger(QueryCloud.class.getName());
    private ArrayList<MapImage> items = null;

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
        Storage storage = StorageOptions.getDefaultInstance().getService();
        items = new ArrayList<>();
        mapImages.forEach(image -> {
            BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, image.getObjectID()).build();
            String url = storage.signUrl(blobInfo, 10, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature()).toString();
            image.setURL(url);
        });
        items = mapImages;
    }


}
