/**
 * Created by My Lap Local on 7/22/2014.
 */
public class Room
{


    private String name;
    private int numUsers;
    private boolean anyUser;
    public Room(String name)
    {
        numUsers=0;
        anyUser=false;
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    public int getNumUsers()
    {
        return numUsers;
    }

    public void setNumUsers(int numUsers)
    {
        this.numUsers = numUsers;
    }
    public void  increaseNumUsers()
    {
        ++numUsers;
    }
    public void  decreaseNumUsers()
    {
        --numUsers;
    }
    public boolean isAnyUser()
    {
        return anyUser;
    }

    public void setAnyUser(boolean anyUser)
    {
        this.anyUser = anyUser;
    }
}
