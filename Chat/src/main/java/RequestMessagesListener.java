import com.corundumstudio.socketio.SocketIOClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

/**
 * Created by My Lap Local on 14/08/2014.
 */
public class RequestMessagesListener implements FutureCallback<HttpResponse>
{
    SocketIOClient client;
    CountDownLatch latch;

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
                        client.sendEvent("message", message.toString());
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
    }

    @Override
    public void failed (Exception ex)
    {

    }

    @Override
    public void cancelled ()
    {

    }

    public void setClient(SocketIOClient client)
    {
        this.client = client;
    }

    public void setLatch(CountDownLatch latch)
    {
        this.latch = latch;
    }
}
