package com.google.step;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Blob.BlobSourceOption;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/query-cloud")
public class QueryCloud extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }


}
