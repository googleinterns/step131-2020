 
package com.google.step;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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
  private String timeStamp;

  /** Represents attributes of a MapImage unique instance (New_York_5x_06_2020.png) and is a name. */
  private String objectID;  

  public MapImage(double longitude, double latitude, String cityName, int zoom, int month, int year, String timeStamp) {
      this.longitude = longitude;
      this.latitude = latitude;
      this.cityName = cityName;
      this.zoom = zoom;
      this.month = month;
      this.year = year;
      this.timeStamp = timeStamp;
  }

  /** Overload the constructor for faster loading & querying from Datastore **/
  public MapImage(double latitude, double longitude, int zoom) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.zoom = zoom;
  }
  
  /** 
  * Create name based off of attributes: year, month, name of city, and zoom level in this order.
  */
  public void setObjectID() {
      String city = cityName.replaceAll(" ", "_");
      objectID = (year + "/" + month  + "/" + city + "/" + zoom + "x.png");
  }

  public void setMonth(int month) {
      this.month = month;
  }

  public void setYear(int year) {
      this.year = year;
  }

  public void setCityName(String cityName) {
      this.cityName = cityName;
  }

  public void setTimeStamp(String timeStamp) {
      this.timeStamp = timeStamp;
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
      return month;
  }

  public int getYear() {
      return year;
  }  

  public String getTimeStamp() {
      return timeStamp;
  }

  public String getObjectID() {
      return objectID;
  }    
}
