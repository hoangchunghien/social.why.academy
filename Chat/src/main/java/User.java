/**
 * Created by My Lap Local on 7/25/2014.
 */
public class User
{
    public User()
    {

    }
    private String name;
    private String id;
    private String picture_url;

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }

    public String getPicture_url()
    {
        return picture_url;
    }
    public void setPicture_url(String picture_url)
    {
        this.picture_url = picture_url;
    }

    //    public boolean equals(Object obj) {
//        if (obj == null) return false;
//        if (obj == this) return true;
//        if (!(obj instanceof User)) return false;
//        User o = (User) obj;
//        return o.id.equals(this.id);
//    }
}
