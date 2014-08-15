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
    //public static final String HOST = "localhost";
    //public static final int PORT = 8081;
    public static final String HOST = "staging.why.academy/";
    public static final int PORT = 8080;
    private SocketIOServer server;
    public ServiceHandler(SocketIOServer server)
    {
        this.server = server;
    }
    //int numUsers =0;
    ConcurrentMap<UUID,String> userIDs = new ConcurrentHashMap<UUID, String>();
    //HashMap<UUID,String> userrooms = new HashMap<UUID, String>();
    //HashMap<String,Integer> numUsersInRoom = new HashMap<String, Integer>();
    //Map<String,UUID> idClients = new HashMap<String, UUID>();
    //List<Room> listRooms = new ArrayList<Room>();
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    ConcurrentMap<String,CopyOnWriteArrayList<SocketIOClient>> clientsOfUser = new
            ConcurrentHashMap<String, CopyOnWriteArrayList<SocketIOClient>>();
    Map<String,List<String>> usersInRoom = new HashMap<String, List<String>>();
    Queue<JsonObject> messagesInGeneral = new ArrayDeque<JsonObject>();

    @OnEvent("log in")
    public void onEventLogInHandler(SocketIOClient client, String data, AckRequest ackRequest)
    {
        System.out.println(data);
        Gson gson = new Gson();
        //client.sendEvent("log in",encodeURIComponent(gson.toJson(users)));

        User user = gson.fromJson(data,User.class);
        //System.out.println(user.toString());
        userIDs.put(client.getSessionId(),user.getId());
        //users.add(user);
        addToUsers(user);
        addToClientsOfUser(client, user);
        //client.set("username",username);
        //idClients.put(username,client.getSessionId());
        //users.add(new User(username));
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

//        String message = jsonObjectData.get("message").getAsString();
//        jsonObjectData.remove(message);
//        JsonObject messageObject = new JsonObject();
//        messageObject.addProperty("message",message);
        //TODO: date time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date now = new Date();
        jsonObjectData.addProperty("time", dateFormat.format(now));
        server.getRoomOperations(roomID).sendEvent("message", encodeURIComponent(jsonObjectData.toString()));

//        jsonObjectData.add("message", messageObject);
//        server.getRoomOperations(roomID).sendEvent("message", jsonObjectData.toString());
        if (roomID.equals("1"))
        {
            messagesInGeneral.add(jsonObjectData);
            if (messagesInGeneral.size()>100)
                messagesInGeneral.remove();
        }

        saveMessages(jsonObjectData);

        //client.sendEvent("message");

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
                    //.replaceAll("\\%28", "(")
                    //.replaceAll("\\%29", ")")
                    .replaceAll("%20","\\+");
                    //.replaceAll("\\%27", "'")
                    //.replaceAll("\\%21", "!")
                    //.replaceAll("\\%7E", "~");
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
            //String message = encodeURIComponent("á!' /  `-a-zA-Z0-9._*~'()!");
            //client.sendEvent("message",message);
            String json = getMessagesFromServer1("1");
//            System.out.println(messagesInGeneral.toString());
//            //server.getRoomOperations(roomID).sendEvent("join", jsonObjectData.toString(),messagesInGeneral.toString());
//            for(JsonObject message:messagesInGeneral)
//            {
//                client.sendEvent("message", message.toString());
//            }

//            String json =getMessagesFromServer("1");//TODO: handle when not connect
            JsonArray messages = gson.fromJson(json, JsonObject.class).
                    getAsJsonArray("messages");
            for(JsonElement message:messages)
            {
                System.out.println(message.toString());

                client.sendEvent("message",  encodeURIComponent(message.toString()));
                //client.sendEvent("message","Tiếng");
//                String abc= message.toString();
//                JsonObject object = gson.fromJson(abc,JsonObject.class);
//                object.remove("user");
//                client.sendEvent("message",object.toString());
            }
            //client.sendEvent("message");
//            for (int i = 0; i < 10; i++)
//            {
//                client.sendEvent("message","AAD");
//            }
        }

//        boolean isContain = false;
//        Room room = new Room(roomName);
//        for(Room r:listRooms)
//        {
//            if (r.getName().equals(roomName))
//            {
//                isContain=true;
//                room = r;
//                break;
//            }
//        }
//        if (!isContain)
//        {
//            //numUsersInRoom.put(room,0);
//            //room =new Room(roomName);
//            listRooms.add(room);
//        }
//        room.increaseNumUsers();
//        room.setAnyUser(true);
//        //anyUser = true;
////        ++numUsers;
//        String username = data.get("username");
//
//        client.set("username",username);
//
//        JsonObject object = new JsonObject();
//        object.addProperty("numUsers",room.getNumUsers());
//        client.sendEvent("login",object.toString());
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("username",username);
//        jsonObject.addProperty("numUsers",room.getNumUsers());
//        for (String rm: client.getAllRooms())
//        {
//            server.getRoomOperations(rm).sendEvent("user joined", jsonObject.toString());
//        }
    }
    public String getMessagesFromServer(String roomID,final SocketIOClient client) throws Exception
    {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(HOST).setPort(PORT).setPath("/messages")
                .setParameter("room", roomID).setParameter("limit[]","2");
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        //CloseableHttpResponse response = httpclient.execute(httpget);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try
        {
            httpclient.start();
//            RequestMessagesListener listener = new RequestMessagesListener();
//            listener.setClient(client);
            final CountDownLatch latch1 = new CountDownLatch(1);
//            listener.setLatch(latch1);
            httpclient.execute(httpget, new FutureCallback<HttpResponse>()
            {
                @Override
                public void completed(HttpResponse result)
                {
                    try
                    {
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
                                //return responseString.toString();
                                String json = responseString.toString();
                                Gson gson = new Gson();
                                JsonArray messages = gson.fromJson(json, JsonObject.class).
                                        getAsJsonArray("messages");
                                for (JsonElement message : messages)
                                {
                                    System.out.println(message);
                                    //client.sendEvent("message", message.toString());
                                }

                            } finally
                            {
                                instream.close();
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        System.out.println("EXCEPTION");
                    }
                    finally
                    {
                        System.out.println("FINISHED");
                        latch1.countDown();
                    }
                }

                @Override
                public void failed(Exception ex)
                {
                    latch1.countDown();
                }

                @Override
                public void cancelled()
                {
                    latch1.countDown();
                }
            });
            latch1.await();
            //latch1.await();
                    //HttpResponse response = future.get();
            //try {
//                HttpEntity entity = response.getEntity();
//                if (entity != null) {
//                    InputStream instream = entity.getContent();
//                    try {
//                        BufferedReader in = new BufferedReader(
//                                new InputStreamReader(instream));
//                        String inputLine;
//                        StringBuffer responseString = new StringBuffer();
//
//                        while ((inputLine = in.readLine()) != null) {
//                            responseString.append(inputLine);
//                        }
//                        System.out.println(responseString);
//                        return responseString.toString();
//
//                    } finally {
//                        instream.close();
//                    }
//                }
//            } finally {
//                response.close();
//            }
        } finally
        {
            httpclient.close();
        }
        return null;
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
            // and wait until a response is received
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
//                    String json = responseString.toString();
//                    Gson gson = new Gson();
//                    JsonArray messages = gson.fromJson(json, JsonObject.class).
//                            getAsJsonArray("messages");
//                    for (JsonElement message : messages)
//                    {
//                        System.out.println(message);
//                        //client.sendEvent("message", message.toString());
//                        return message.toString();
//                    }

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

//        String roomName = client.get("room");
//        Room room = new Room(roomName);
//        for(Room r:listRooms)
//        {
//            if (r.getName().equals(roomName))
//            {
//                //isContain=true;
//                room = r;
//                break;
//            }
//        }
//        if (room.isAnyUser())
//        {
//
//            String user = client.get("username");
//
//            room.decreaseNumUsers();
//
//            idClients.remove(user);
//            //TODO: for loop
//            //users.remove(new User(user));
//            for (String r: client.getAllRooms())
//            {
//                //server.getRoomOperations(r).sendEvent("user left", users);
//            }
//        }

    }

    @OnEvent("private chat")
    public void onEventPrivateChatHandler(SocketIOClient client, String receiverID, AckRequest ackRequest)
            throws Exception
    {
        String senderID = userIDs.get(client.getSessionId());
//        String roomID;
//        if (senderID.compareTo(receiverID)<0)
//            roomID = senderID + receiverID;
//        else
//        {
//            roomID = receiverID+senderID;
//        }
        String str = getMessagesDirectChatFromServer(senderID,receiverID);
        Gson gson = new Gson();
        JsonArray jsonArrayMessages = gson.fromJson(str,JsonObject.class).getAsJsonArray("messages");
        String roomID = jsonArrayMessages.get(0).getAsJsonObject().getAsJsonObject("room").get("id").getAsString();
        System.out.println(roomID);

//        joinUserToRoom(senderID,roomID);
        client.joinRoom(roomID);
        joinUserToRoom(receiverID,roomID);
//        JsonObject room = new JsonObject();
//        room.addProperty("id", roomID);
//        JsonObject data = new JsonObject();
//        data.add("room",room);
//        System.out.println(data.toString());
//        server.getRoomOperations(roomID).sendEvent("private chat offer",data.toString());
        joinUserToRoom(senderID,roomID);
        client.sendEvent("private chat",roomID,encodeURIComponent(str));
//        JsonArray messages = gson.fromJson(str, JsonObject.class).
//                getAsJsonArray("messages");
//        for(JsonElement message:messages)
//        {
//            System.out.println(message.toString());
//            client.sendEvent("message", encodeURIComponent(message.toString()));
//        }



//        if (idClients.get(privateChatUsername)==null)
//        {
//            client.sendEvent("offline",privateChatUsername);
//            System.out.println("offline");
//        }
//        else
//        {
//            String username = client.get("username");
//            String roomkq;
//            if (username.compareTo(privateChatUsername)<0)
//            {
//                roomkq = username+"chatwith"+privateChatUsername;
//            }
//            else
//            {
//                roomkq = privateChatUsername+"chatwith"+username;
//            }
//            Map<String,String> data = new HashMap<String, String>();
//            data.put("username",username);
//            data.put("room",roomkq);
//            server.getClient(idClients.get(privateChatUsername)).sendEvent("private chat offer",data);
//            data.put("username",privateChatUsername);
//            client.sendEvent("private chat offer",data);
//            System.out.println("online");
//        }
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
//                    String json = responseString.toString();
//                    Gson gson = new Gson();
//                    JsonArray messages = gson.fromJson(json, JsonObject.class).
//                            getAsJsonArray("messages");
//                    for (JsonElement message : messages)
//                    {
//                        System.out.println(message);
//                        //client.sendEvent("message", message.toString());
//                        return message.toString();
//                    }

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
