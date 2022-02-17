package geekbrains.myCloud.netty;

import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Map;

public class CloudServer extends BaseNettyServer {
    public CloudServer() {
        super(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new CloudServerHandler()
        );
    }

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();

        if (env.get("MONGO_CONNECTION_STRING") == null) {
            System.out.println("No connection string (MONGO_CONNECTION_STRING) in env");
            return;
        }

        if (env.get("JWT_SECRET") == null) {
            System.out.println("No JWT secret (JWT_SECRET) in env");
            return;
        }

        new CloudServer();
    }
}
