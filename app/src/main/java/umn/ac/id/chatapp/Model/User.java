package umn.ac.id.chatapp.Model;

public class User {

    private String id;
    private String username;
    private String displayname;
    private String imageURL;
    private String birthday;
    private String status;
    public Long time;


    public User(String id, String username,String displayname, String imageURL, String birthday, String status, Long time) {
        this.id = id;
        this.username = username;
        this.displayname = displayname;
        this.imageURL = imageURL;
        this.birthday = birthday;
        this.status = status;
        this.time = time;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
