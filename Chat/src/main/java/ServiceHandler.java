import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by My Lap Local on 7/22/2014.
 */
public class ServiceHandler
{
//    public static final String HOST = "localhost";
//    public static final int PORT = 8081;
//    public static final String HOST = "staging.why.academy";
//    public static final int PORT = 8080;
    public static final String HOST = "api.why.academy";
    public static final int PORT = 80;
    private SocketIOServer server;
    public ServiceHandler(SocketIOServer server)
    {
        this.server = server;
    }
    ConcurrentMap<UUID,String> userIDs = new ConcurrentHashMap<UUID, String>();
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    ConcurrentMap<String,CopyOnWriteArrayList<SocketIOClient>> clientsOfUser = new
            ConcurrentHashMap<String, CopyOnWriteArrayList<SocketIOClient>>();


    @OnEvent("log in")
    public void onEventLogInHandler(SocketIOClient client, String data, AckRequest ackRequest)
    {
        System.out.println(data);
        Gson gson = new Gson();
        User user = gson.fromJson(data,User.class);
        userIDs.put(client.getSessionId(),user.getId());
        addToUsers(user);
        addToClientsOfUser(client, user);
        client.sendEvent("log in",encodeURIComponent(gson.toJson(users)));
        server.getBroadcastOperations().sendEvent("new user login",encodeURIComponent(gson.toJson(users)));

        System.out.println(gson.toJson(users));
    }

    private void addToClientsOfUser(SocketIOClient client, User user)
    {
        CopyOnWriteArrayList<SocketIOClient> clients = clientsOfUser.get(user.getId());
        if (clients!=null)
        {
            clients.add(client);
        }
        else
        {
            clients = new CopyOnWriteArrayList<SocketIOClient>();
            clients.add(client);
            clientsOfUser.put(user.getId(), clients);
        }
    }

    private void addToUsers(User user)
    {
        boolean hasAlready=false;
        for(User u:users)
        {
            if (u.getId().equals(user.getId()))
            {
                hasAlready=true;
            }
        }
        if (!hasAlready)
        {
            //server.getBroadcastOperations().sendEvent("new user login",encodeURIComponent(user.toString()));
            users.add(user);
        }
    }

    @OnEvent("send")
    public void onEventSendHandler(SocketIOClient client, String data, AckRequest ackRequest) throws Exception
    {
        data = decodeURIComponent(data);
        System.out.println(client.getAllRooms().toString());
        System.out.println(data);
        Gson gson = new Gson();
        JsonObject jsonObjectData = gson.fromJson(data,JsonObject.class);
        String roomID = jsonObjectData.getAsJsonObject("room").get("id").getAsString();

        //TODO: date time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date now = new Date();
        jsonObjectData.addProperty("time", dateFormat.format(now));
        server.getRoomOperations(roomID).sendEvent("message", encodeURIComponent(jsonObjectData.toString()));
        saveMessages(jsonObjectData);

    }
    public void saveMessages(JsonObject jsonObject) throws Exception
    {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(HOST).setPort(PORT).setPath("/messages");
        URI uri = builder.build();
        HttpPost post = new HttpPost(uri);
        JsonObject message = new JsonObject();
        message.add("message",jsonObject);
        StringEntity body = new StringEntity(URLEncoder.encode(message.toString(),"UTF-8"));
        post.setEntity(body);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(post);
        if (response.getStatusLine().getStatusCode() != 201)
            throw new Exception();
    }

    public String encodeURIComponent(String component)
    {
        String result = null;

        try
        {
            result = URLEncoder.encode(component, "UTF-8")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%7E", "~");
        }
        catch (UnsupportedEncodingException e)
        {
                result = component;
        }

            return result;
    }
    public String decodeURIComponent(String component)
    {
        String result = null;

        try
        {
            component = component
                    .replaceAll("%20","\\+");
            result =   URLDecoder.decode(component, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            result = component;
        }

        return result;
    }
    @OnEvent("join")
    public void onEventJoinHandler(final SocketIOClient client, String data, AckRequest ackRequest) throws Exception
    {
        System.out.println(data);
        Gson gson = new Gson();
        JsonObject jsonObjectData = gson.fromJson(data,JsonObject.class);
        String userID = jsonObjectData.getAsJsonObject("user").get("id").getAsString();
        String roomID = jsonObjectData.getAsJsonObject("room").get("id").getAsString();
        joinUserToRoom(userID, roomID);
        server.getRoomOperations(roomID).sendEvent("join", jsonObjectData.toString());
        if (roomID.equals("1"))
        {
            String json = getMessagesFromServer1("1");
            JsonArray messages = gson.fromJson(json, JsonObject.class).
                    getAsJsonArray("messages");
            for(JsonElement message:messages)
            {
                System.out.println(message.toString());
                client.sendEvent("message",  encodeURIComponent(message.toString()));
            }
        }
    }
    public String getMessagesFromServer1(String roomID) throws Exception
    {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(HOST).setPort(PORT).setPath("/messages")
                .setParameter("room", roomID).setParameter("limit[]","20");
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try
        {
            httpclient.start();
            Future<HttpResponse> future = httpclient.execute(httpget, null);
            HttpResponse result = future.get();
            HttpEntity entity = result.getEntity();
            if (entity != null)
            {
                InputStream instream = entity.getContent();
                try
                {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(instream));
                    String inputLine;
                    StringBuffer responseString = new StringBuffer();

                    while ((inputLine = in.readLine()) != null)
                    {
                        responseString.append(inputLine);
                    }
                    System.out.println(responseString);
                    return responseString.toString();

                } finally
                {
                    instream.close();
                }
            }

        }
        finally
        {
            httpclient.close();
        }
        return null;
    }
    private void joinUserToRoom(String userID, String roomID)
    {
        List<SocketIOClient> list = clientsOfUser.get(userID);
        if (list!=null)
        {
            for (SocketIOClient clientSocket : list)
            {
                clientSocket.joinRoom(roomID);
            }
        }
    }

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient client)
    {
        String userID = userIDs.get(client.getSessionId());
        if (userID!=null)
        {
            CopyOnWriteArrayList<SocketIOClient> clients = clientsOfUser.get(userID);
            if (clients != null)
            {
                clients.remove(client);
                if (clients.size() == 0)
                {
                    clientsOfUser.remove(userID);
                    for (User u : users)
                    {
                        if (u.getId().equals(userID))
                        {
                            Gson gson = new Gson();
                            server.getBroadcastOperations().sendEvent("user disconnect", encodeURIComponent(gson.toJson(u)));
                            users.remove(u);
                        }
                    }
                    //server.getBroadcastOperations().sendEvent("part", users);
                }
            }
        }

    }

    @OnEvent("private chat")
    public void onEventPrivateChatHandler(SocketIOClient client, String receiverID, AckRequest ackRequest)
            throws Exception
    {
        String senderID = userIDs.get(client.getSessionId());
        String str = getMessagesDirectChatFromServer(senderID,receiverID);
        Gson gson = new Gson();
        JsonArray jsonArrayMessages = gson.fromJson(str,JsonObject.class).getAsJsonArray("messages");
        String roomID = jsonArrayMessages.get(0).getAsJsonObject().getAsJsonObject("room").get("id").getAsString();
        System.out.println(roomID);

        client.joinRoom(roomID);
        joinUserToRoom(receiverID,roomID);
        joinUserToRoom(senderID,roomID);
        client.sendEvent("private chat",roomID,encodeURIComponent(str));
    }
    public String getMessagesDirectChatFromServer(String senderID,String receiverID) throws Exception
    {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(HOST).setPort(PORT).setPath("/messages")
                .setParameter("userID",senderID+","+receiverID).setParameter("limit[]","20");
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response =  httpclient.execute(httpget);
        try
        {
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                InputStream instream = entity.getContent();
                try
                {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(instream));
                    String inputLine;
                    StringBuffer responseString = new StringBuffer();

                    while ((inputLine = in.readLine()) != null)
                    {
                        responseString.append(inputLine);
                    }
                    System.out.println(responseString);
                    return responseString.toString();
                } finally
                {
                    instream.close();
                }
            }

        }
        finally
        {
            httpclient.close();
        }
        return null;
    }
}
