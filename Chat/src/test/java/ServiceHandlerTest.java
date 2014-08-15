import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ServiceHandlerTest
{
    ServiceHandler serviceHandler;// = new ServiceHandler(new SocketIOServer(new Configuration()));
    @Before
    public void setUp()
    {
        serviceHandler = new ServiceHandler(new SocketIOServer(new Configuration()));
    }

    @Test
    public void getMessagesFromServerTest() throws Exception
    {
        //serviceHandler.getMessagesFromServer("1");
    }

    @Test
    public void saveMessageToServerTest() throws Exception
    {
        JsonObject message = new JsonObject();
        message.addProperty("message", "test");
        //Date now = new Date();
        //message.addProperty("time",now.toString());

        JsonObject room = new JsonObject();
        room.addProperty("id", 1);
        room.addProperty("name", "general");
        message.add("room",room);

        JsonObject user = new JsonObject();
        user.addProperty("id", "201");
        user.addProperty("username", "Viet Vo Hac");
        message.add("user", user);


        //JsonObject body = new JsonObject();
        //body.add("message", message);
        //String data ="{\"message\":\"test\",\"room\":{\"id\":1,\"name\":\"general\"},\"user\":{\"id\":\"1\",\"username\":\"Hiền Hoàng\"}}";
        //String data = "{\"message\":\"abcdefg\",\"user\":{\"id\":\"1\",\"username\":\"Hiá»\u0081n HoÃ ng\"},\"room\":{\"id\":1}}";
        //Gson gson = new Gson();
        //JsonObject object = gson.fromJson(data,JsonObject.class);
        serviceHandler.saveMessages(message);
    }
    @Test
    public void getMessagesDirectChatFromServerTest() throws Exception
    {
        String result = serviceHandler.getMessagesDirectChatFromServer("1","2");
        System.out.println(result);
    }
}