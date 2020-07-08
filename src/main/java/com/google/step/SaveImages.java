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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;

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
import java.lang.StringBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.apphosting.api.DeadlineExceededException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


@WebServlet("/save-images-job")
public class SaveImages extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String BUCKET_NAME = String.format("%s.appspot.com", PROJECT_ID);
    private Date date = new Date();
    private String month = String.format("%tm", date);
    private String year = String.format("%TY", date);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        ArrayList<MapImage> mapImages = gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>(){}.getType());  
        ArrayList<String> requestUrls = generateRequestUrls(mapImages);

        // There are an equal number of elements in mapImages & requestUrls
        for(int i = 0; i < mapImages.size(); i++) {
            try {
                byte[] imageData = getImageData(requestUrls.get(i));
                saveImageToCloudStorage(imageData, mapImages.get(i));
            }
            // TODO: add error handling
            catch(DeadlineExceededException e) {}

        }

        // Send the mapImages to MapImageDatastore.java after setting the metadata
        Gson sendGson = new Gson();
        URL url = new URL("https://map-snapshot-step.uc.r.appspot.com/save-datastore");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        String data = sendGson.toJson(mapImages);
        con.setRequestProperty("Content-Length", Integer.toString(data.length()));
        try(DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        }
        // TODO: add logging
        catch(IOException e) {}
        // Consume the InputStream
        con.getInputStream().close();

    }

    private byte[] getImageData(String requestURL) throws IOException {
        URL url = new URL(requestURL);
        BufferedImage image = ImageIO.read(url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        return bos.toByteArray();
    }

    private void saveImageToCloudStorage(byte[] imageData, MapImage mapImage) throws StorageException {
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
        mapImage.setMonth(Integer.parseInt(month));
        mapImage.setYear(Integer.parseInt(year));
        mapImage.setObjectID();
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy K:mm a");
        mapImage.setTimeStamp(time.format(formatter));
        BlobId blobId = BlobId.of(BUCKET_NAME, mapImage.getObjectID());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
        Blob blob = storage.create(blobInfo, imageData);
    }

    private ArrayList<String> generateRequestUrls (ArrayList<MapImage> mapImages) {
        ArrayList<String> requestUrls = new ArrayList<>();
        for(MapImage mapImage : mapImages) {
            StringBuilder requestUrl = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");
            requestUrl.append("center=" + mapImage.getLatitude() + "," + mapImage.getLongitude());
            requestUrl.append("&zoom=" + mapImage.getZoom());
            requestUrl.append("&size=640x640");
            requestUrl.append("&scale=2");
            // API key will be reset and removed from code in future commit
            requestUrl.append("&key=AIzaSyA75DbMo0voP63IzAQykD1xXhPEI8_F984");
            requestUrls.add(requestUrl.toString());
        }
        return requestUrls;
    }
}
