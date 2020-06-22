package com.google.step;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;

@WebServlet("/query-drive")
public class QueryDrive extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if(session != null) {
            String accessToken = (String)session.getAttribute("accessToken");
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            Drive drive = new Drive.Builder(new UrlFetchTransport(), new JacksonFactory(), credential).setApplicationName("Map Stuff").build();
            List<File> result = new ArrayList<File>();
            Files.List fileRequest = drive.files().list().setDriveId("0AJnQ8N4V8NrAUk9PVA").setIncludeItemsFromAllDrives(true).setCorpora("drive").setSupportsAllDrives(true);

            do {
                try {
                    FileList files = fileRequest.execute();

                    result.addAll(files.getFiles());
                    fileRequest.setPageToken(files.getNextPageToken());
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e);
                    fileRequest.setPageToken(null);
                }
            } while (fileRequest.getPageToken() != null && fileRequest.getPageToken().length() > 0);

            result.forEach(file -> {
                System.out.println(file.getName());
            });
        }
    }


}
