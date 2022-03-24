package com.example.dreams.Model;

public class Comments {
    private String username,profileImageUrl,comment;

    public Comments() {
    }

    public Comments(String username, String profileImageUrl, String comment) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
