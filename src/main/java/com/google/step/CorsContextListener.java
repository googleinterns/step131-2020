package com.google.step;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Cors;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * This servlet is a context listener that runs at application startup. It configures the Cloud
 * Storage bucket to accept CORS requests from the project URL.
 */
@WebListener
public class CorsContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(CommonUtils.BUCKET_NAME);
        String origin = "https://map-snapshot-step.uc.r.appspot.com";
        HttpMethod method = HttpMethod.GET;
        String responseHeader = "Content-Type";
        int maxAgeSeconds = 3600;

        Cors cors =
                Cors.newBuilder()
                        .setOrigins(ImmutableList.of(Cors.Origin.of(origin)))
                        .setMethods(ImmutableList.of(method))
                        .setResponseHeaders(ImmutableList.of(responseHeader))
                        .setMaxAgeSeconds(maxAgeSeconds)
                        .build();

        bucket.toBuilder().setCors(ImmutableList.of(cors)).build().update();
    }
}
