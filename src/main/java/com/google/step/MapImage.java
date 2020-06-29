 
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

/**
 * Class representing a map snapshot image and its metadata.
 */
public class MapImage {

  /** Snapshot's longitude coordinate. */
  private double longitude;

  /** Snapshot's latitude coordinate. */
  private double latitude;

  /** Snapshot's cityName coordinate. */
  private String cityName;

  /** Snapshot's zoom coordinate. */
  private int zoom;

  /** Month snapshot was took. */
  private int month;

  /** Year snapshot was took. */
  private int year;

  /** Exact time snapshot was took. */
  private int timeStamp;

  /** Represents attributes of a MapImage unique instance (New_York_5x_06_2020.png) and is a name. */
  private String objectID;  

  public MapImage(double longitude, double latitude, String cityName, int zoom, int month, int year, int timeStamp) {
      this.longitude = longitude;
      this.latitude = latitude;
      this.cityName = cityName;
      this.zoom = zoom;
      this.month = month;
      this.year = year;
      this.timeStamp = timeStamp;
  }
  
  /** 
  * Create name based off of attributes: year, month, name of city, and zoom level in this order.
  */
  public void setObjectID() {
      String city = cityName.replaceAll(" ", "_");
      objectID = (year + "/" + month  + "/" + city + "/" + zoom + "x.png");
  }

  public double getLongitude() {
      return longitude;
  }

  public double getLatitude() {
      return latitude;
  }
  public String getCityName() {
      return cityName;
  }

  public int getZoom() {
      return zoom;
  }

  public int getMonth() {
      return longitude;
  }

  public int getYear() {
      return year;
  }  

  public int getTimeStamp() {
      return timeStamp;
  }

  public String getObjectID() {
      return objectID;
  }    
}
