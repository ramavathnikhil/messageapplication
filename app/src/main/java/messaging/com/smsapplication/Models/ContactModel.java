package messaging.com.smsapplication.Models;

import android.graphics.Bitmap;


public class ContactModel {

    private String contactName;
    private Bitmap profilePic;

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }
}
