import java.io.IOException;
import java.net.ServerSocket;

public class RestServer {

    private int port;
    private String directory;

    public RestServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    void start() {
        try (var server = new ServerSocket(this.port)) {
            while (true) {
                var socket = server.accept();
                var thread = new RestServerHandler(socket, this.directory);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConfigLoader.loadConfigENV("src\\resources\\config.properties");
        new RestServer(Integer.parseInt(System.getenv().get("port")), System.getenv().get("directory")).start();

    }
}



