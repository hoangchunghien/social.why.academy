import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import java.io.InputStream;

/**
 * Created by My Lap Local on 7/16/2014.
 */
public class Server
{
    public static void main(String[] args) throws Exception
    {
        Configuration config = new Configuration();
        //config.setHostname("localhost");
        config.setPort(8080);
        //config.setKeyStorePassword("test1234");
        //InputStream stream = Server.class.getResourceAsStream("/keystore.jks");
        //config.setKeyStore(stream);
        final SocketIOServer server = new SocketIOServer(config);
        server.addListeners(new ServiceHandler(server));
        server.start();
        Thread.sleep(Integer.MAX_VALUE);
        server.stop();
    }
}
