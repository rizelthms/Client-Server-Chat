package protocoltests.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import server_level2.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerTestExtension implements BeforeEachCallback, AfterEachCallback {

    private final static Properties PROPS = new Properties();

    private Server server;

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        InputStream in = ServerTestExtension.class.getResourceAsStream("../testconfig.properties");
        PROPS.load(in);
        if (in != null) {
            in.close();
        }
        server = new Server(Integer.parseInt(PROPS.getProperty("port")), 1338);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        server.closeServer();
    }
}