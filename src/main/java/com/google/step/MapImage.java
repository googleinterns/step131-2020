package com.google.step;

/** Class representing a map snapshot image and its metadata. */
public class MapImage {
    // TODO(cgregori): Remove all references to FAKE_CRON_MONTH by Aug 1.
    /** Sentinel month for sub-daily cron jobs to prevent duplicates */
    static final int FAKE_CRON_MONTH = 13;
  
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

    /** Exact time snapshot was took from Unix Epoch in seconds. */
    private long timeStamp;

    /**
     * Represents attributes of a MapImage unique instance (New_York_5x_06_2020.png) and is a name.
     */
    private String objectID;

    /** URL given by Cloud to display the image. */
    private String url;

    public MapImage(
            double longitude,
            double latitude,
            String cityName,
            int zoom,
            int month,
            int year,
            long timeStamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.cityName = cityName;
        this.zoom = zoom;
        this.month = month;
        this.year = year;
        this.timeStamp = timeStamp;
    }

    public MapImage (double longitude, double latitude, String cityName, int zoom) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityName = cityName;
        this.zoom = zoom;
    }

    /** Overload the constructor for faster loading & querying from Datastore. * */
    public MapImage(double latitude, double longitude, int zoom) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
    }

    /**
     * Sets object's ID based off of attributes: year, month, name of city, and zoom level in this
     * order.
     */
    public void setObjectID() {
        String city = cityName.replaceAll(" ", "_").replaceAll(",", "_");
        objectID = (year + "/" + month + "/" + city + "/" + zoom + "x.png");
    }

    /** Sets image's URL created by Cloud. */
    public void setURL(String gcsURL) {
        url = gcsURL;
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

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public void setTimeStamp(long timeStamp) {
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getObjectID() {
        return objectID;
    }

    public String getURL() {
        return url;
    }
}
