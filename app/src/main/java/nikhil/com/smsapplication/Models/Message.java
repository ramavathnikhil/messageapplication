package nikhil.com.smsapplication.Models;

/**
 * Created by Nikil on 1/26/2017.
 */
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
