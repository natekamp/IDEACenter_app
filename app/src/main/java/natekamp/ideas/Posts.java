package natekamp.ideas;

//used for the FirebaseRecyclerAdapter
public class Posts
{
    private String UID, Timestamp, Title, Description, Attachment, Profile_Picture, Username;

    public Posts() {}

    public Posts(String UID, String timestamp, String title, String description, String attachment, String profile_Picture, String username)
    {
        this.UID = UID;
        Timestamp = timestamp;
        Title = title;
        Description = description;
        Attachment = attachment;
        Profile_Picture = profile_Picture;
        Username = username;
    }

    public String getUID(){
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getAttachment() {
        return Attachment;
    }

    public void setAttachment(String attachment) {
        Attachment = attachment;
    }

    public String getProfile_Picture() {
        return Profile_Picture;
    }

    public void setProfile_Picture(String profile_Picture) {
        Profile_Picture = profile_Picture;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
