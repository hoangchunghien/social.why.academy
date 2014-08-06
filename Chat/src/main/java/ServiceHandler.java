import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by My Lap Local on 7/22/2014.
 */
public class ServiceHandler
{
    private SocketIOServer server;
    public ServiceHandler(SocketIOServer server)
    {
        this.server = server;
    }
    //int numUsers =0;
    HashMap<UUID,String> userIDs = new HashMap<UUID, String>();
    //HashMap<UUID,String> userrooms = new HashMap<UUID, String>();
    //HashMap<String,Integer> numUsersInRoom = new HashMap<String, Integer>();
    //Map<String,UUID> idClients = new HashMap<String, UUID>();
    //List<Room> listRooms = new ArrayList<Room>();
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    Map<String,List<SocketIOClient>> clientsOfUser = new HashMap<String, List<SocketIOClient>>();
    Map<String,List<String>> usersInRoom = new HashMap<String, List<String>>();
    @OnEvent("log in")
    public void onEventLogInHandler(SocketIOClient client, String data, AckRequest ackRequest)
    {

        System.out.println(data);
        client.sendEvent("log in",users);
        Gson gson = new Gson();
        User user = gson.fromJson(data,User.class);
        userIDs.put(client.getSessionId(),user.getId());
        //users.add(user);
        addToUsers(user);
        addToClientsOfUser(client, user);
        //client.set("username",username);
        //idClients.put(username,client.getSessionId());
        //users.add(new User(username));
        //server.getBroadcastOperations().sendEvent("log in",users);
        server.getBroadcastOperations().sendEvent("new user login",user);
        System.out.println(gson.toJson(users));
    }

    private void addToClientsOfUser(SocketIOClient client, User user)
    {
        List<SocketIOClient> clients = clientsOfUser.get(user.getId());
        if (clients!=null)
        {
            clients.add(client);
        }
        else
        {
            clients = new ArrayList<SocketIOClient>();
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
            users.add(user);
    }

    @OnEvent("send")
    public void onEventSendHandler(SocketIOClient client, String data, AckRequest ackRequest)
    {
        System.out.println(client.getAllRooms().toString());
        System.out.println(data);
        Gson gson = new Gson();
        JsonObject jsonObjectData = gson.fromJson(data,JsonObject.class);
        String roomID = jsonObjectData.getAsJsonObject("room").get("room_id").getAsString();

        String message = jsonObjectData.get("message").getAsString();
        jsonObjectData.remove(message);
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("message",message);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        messageObject.addProperty("time",dateFormat.format(now));
        jsonObjectData.add("message",messageObject);

        server.getRoomOperations(roomID).sendEvent("message", jsonObjectData.toString());
    }
    @OnEvent("join")
    public void onEventJoinHandler(SocketIOClient client, String data, AckRequest ackRequest)
    {
        System.out.println(data);
        Gson gson = new Gson();
        JsonObject jsonObjectData = gson.fromJson(data,JsonObject.class);
        String userID = jsonObjectData.getAsJsonObject("user").get("id").getAsString();
        String roomID = jsonObjectData.getAsJsonObject("room").get("room_id").getAsString();
        joinUserToRoom(userID, roomID);

        server.getRoomOperations(roomID).sendEvent("join", jsonObjectData.toString());
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

    private void joinUserToRoom(String userID, String roomID)
    {
        List<SocketIOClient> list = clientsOfUser.get(userID);
        for (SocketIOClient clientSocket:list)
        {
            clientSocket.joinRoom(roomID);
        }
    }

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient client)
    {
        String userID = userIDs.get(client.getSessionId());
        List<SocketIOClient> clients = clientsOfUser.get(userID);
        if (clients!=null)
        {
            clients.remove(client);
            if (clients.size() == 0)
            {
                clientsOfUser.remove(userID);
                for (User u : users)
                {
                    if (u.getId().equals(userID))
                    {
                        server.getBroadcastOperations().sendEvent("part", u);
                        users.remove(u);
                    }
                }
                //server.getBroadcastOperations().sendEvent("part", users);
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
    {
        String senderID = userIDs.get(client.getSessionId());
        String roomID;
        if (senderID.compareTo(receiverID)<0)
            roomID = senderID + receiverID;
        else
        {
            roomID = receiverID+senderID;
        }
//        joinUserToRoom(senderID,roomID);
        client.joinRoom(roomID);
        joinUserToRoom(receiverID,roomID);
        JsonObject room = new JsonObject();
        room.addProperty("room_id", roomID);
        JsonObject data = new JsonObject();
        data.add("room",room);
        System.out.println(data.toString());
        server.getRoomOperations(roomID).sendEvent("private chat offer",data.toString());
        joinUserToRoom(senderID,roomID);
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
}
