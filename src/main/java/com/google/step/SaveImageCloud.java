package com.google.step;

import com.google.apphosting.api.DeadlineExceededException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet stores Static Maps binary image data in Cloud. A POST request gets MapImages to make
 * Static Maps URL requests and store the binary image data in Cloud.
 */
@WebServlet(
        name = "SaveToGCS",
        description = "taskqueue: Save images to GCS",
        urlPatterns = "/save-images-cloud-job")
public class SaveImageCloud extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SaveImageCloud.class.getName());

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        ArrayList<MapImage> mapImages =
                gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>() {}.getType());
        ArrayList<String> requestUrls = generateRequestUrls(mapImages);
        Storage storage =
                StorageOptions.newBuilder()
                        .setProjectId(CommonUtils.PROJECT_ID)
                        .build()
                        .getService();
        // Get each MapImage image data from Static Maps and send it to Cloud Storage.
        for (int i = 0; i < mapImages.size(); i++) {
            try {
                MapImage mapImage = mapImages.get(i);
                byte[] imageData = getImageData(requestUrls.get(i));
                mapImage.updateMetadata(LocalDateTime.now());
                saveImageToCloudStorage(storage, imageData, mapImage);
            } catch (DeadlineExceededException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                throw e;
            }
        }

        // Send the MapImages to SaveMapImageDatastore.java after setting the metadata.
        Gson sendGson = new Gson();
        URL url = new URL("https://map-snapshot-step.uc.r.appspot.com/save-mapimage-datastore");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        String data = sendGson.toJson(mapImages);
        con.setRequestProperty("Content-Length", Integer.toString(data.length()));
        try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
            writer.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        // Consume the InputStream
        con.getInputStream().close();
    }

    /* This method retrieves the image data from a given request URL.  */
    public byte[] getImageData(String requestURL) throws IOException {
        try {
            URL url = new URL(requestURL);
            BufferedImage image = ImageIO.read(url);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        }
    }

    /* This method saves an image to Cloud Storage. */
    public Blob saveImageToCloudStorage(Storage storage, byte[] imageData, MapImage mapImage)
            throws StorageException, IllegalArgumentException {
        try {
            if (imageData == null) throw new IllegalArgumentException("Image data is null");
            BlobId blobId = BlobId.of(CommonUtils.BUCKET_NAME, mapImage.getObjectID());
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
            Blob blob = storage.create(blobInfo, imageData);
            return blob;
        } catch (StorageException | IllegalArgumentException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        }
    }

    /* This method generates the request URLs for the Static Maps API. */
    public ArrayList<String> generateRequestUrls(ArrayList<MapImage> mapImages) {
        ArrayList<String> requestUrls = new ArrayList<>();
        for (MapImage mapImage : mapImages) {
            StringBuilder requestUrl =
                    new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?");
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
