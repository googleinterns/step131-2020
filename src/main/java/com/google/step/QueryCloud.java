package com.google.step;

import com.google.auth.ServiceAccountSigner.SigningException;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet retrieves binary image data from Cloud for corresponding MapImage instance. A POST
 * request gets image data to convert to a URL then sets the 'url' attribute of its MapImage
 * instance.
 */
@WebServlet("/query-cloud")
public class QueryCloud extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(QueryCloud.class.getName());

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            BufferedReader reader = request.getReader();
            Gson gson = new Gson();
            ArrayList<MapImage> mapImages =
                    gson.fromJson(reader, new TypeToken<ArrayList<MapImage>>() {}.getType());
            Storage storage = StorageOptions.getDefaultInstance().getService();
            mapImages.forEach(
                    image -> {
                        String url = CommonUtils.getCloudFileURL(storage, image.getObjectID(), 30).toString();
                        image.setURL(url);
                    });
            String data = gson.toJson(mapImages);
            response.setContentType("application/json");
            response.getWriter().println(data);
        } catch (SigningException e) {
            LOGGER.severe(e.getCause().getMessage());
        }
    }
}
