package umn.ac.id.chatapp.Model;

import java.util.Map;

public class Chatlist {

    public String id;
    public Long time;

    public Chatlist(String id, Long time) {
        this.id = id;
        this.time = time;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
