package com.wilsonundrix.watered;

import java.util.Date;

public class IdeaPosts extends IdeaPostId{
    private String title, description, image_uri, user_id;
    private Date timestamp;

    public IdeaPosts() {
    }

    public IdeaPosts(String title, String description, String image_uri, String user_id, Date timestamp) {
        this.title = title;
        this.description = description;
        this.image_uri = image_uri;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
