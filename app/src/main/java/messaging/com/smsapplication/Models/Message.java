package messaging.com.smsapplication.Models;


public class Message {



    private String user,message,createdAt;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
