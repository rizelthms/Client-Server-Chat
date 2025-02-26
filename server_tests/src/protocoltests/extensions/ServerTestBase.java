package protocoltests.extensions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@ExtendWith(ServerTestExtension.class)
public abstract class ServerTestBase {
    protected final static Properties PROPS = new Properties();

    protected final static int MAX_DELTA_ALLOWED_MS = 100;

    private Socket socketUser1;
    private Socket socketUser2;
    private Socket socketUser3;
    protected BufferedReader inUser1;
    protected BufferedReader inUser2;
    protected BufferedReader inUser3;
    protected PrintWriter outUser1;
    protected PrintWriter outUser2;
    protected PrintWriter outUser3;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = ServerTestExtension.class.getResourceAsStream("../testconfig.properties");
        PROPS.load(in);
        if (in != null) {
            in.close();
        }
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        socketUser2 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        socketUser3 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        inUser3 = new BufferedReader(new InputStreamReader(socketUser3.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
        outUser3 = new PrintWriter(socketUser3.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
        socketUser3.close();
    }

    protected String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }
}
