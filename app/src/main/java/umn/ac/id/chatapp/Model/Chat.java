package umn.ac.id.chatapp.Model;

import java.util.Map;

public class Chat {


    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private boolean isimage;
    private Long time;
    private boolean isunsend;

    public Chat(String sender, String receiver, String message, boolean issen, boolean isimage, Long time, boolean isunsend) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = issen;
        this.isimage = isimage;
        this.time = time;
        this.isunsend = isunsend;
    }

    public Chat() {
    }

    public boolean isIsunsend() {
        return isunsend;
    }

    public void setIsunsend(boolean isunsend) {
        this.isunsend = isunsend;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public boolean isIsimage() {
        return isimage;
    }

    public void setIsimage(boolean isimage) {
        this.isimage = isimage;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
