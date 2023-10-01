package com.example.geodo;

import java.util.Calendar;
import java.util.Date;

public class Model {
    private String objectId;
    private Date createdAt;
    private String id;
    private String title;
    private String description;
    private String userName;
    private Double latitude;
    private Double longitude;
    private Date dueDate;
    private String image;

    public Model(){

    }

    public Model(String objectId, Date createdAt, String id, String title, String description, String userName, Double latitude, Double longitude, Date dueDate, String image) {
        this.objectId = objectId;
        this.createdAt = createdAt;
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userName = userName;
        this.dueDate = dueDate;
        this.image = image;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public Double getLatitude() {

        return latitude == null ? 0.0 : latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude == null ? 0.0 : longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {

        return title == null ? "" : title;
    }

    public String getDescription() {

        return description == null ? "" : description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDueDate() {
        if(dueDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            dueDate = calendar.getTime();
            return dueDate;
        }
        else{
            return dueDate;
        }
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getImage() {
        return image == null ? "" : image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
